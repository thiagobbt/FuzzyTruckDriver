package remoteDriver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.Rule;
import net.sourceforge.jFuzzyLogic.rule.Variable;
 
public class RemoteDriver {
	
	static int port = 4321;
	static String host = "localhost";
	
    public static void main(String[] args) throws IOException {

        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }

        System.out.println("Will connect to port " + port);
        Socket kkSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;
 
        try {
            kkSocket = new Socket(host, port);
            out = new PrintWriter(kkSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:"  + host);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + host);
            System.exit(1);
        }
 
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String fromServer;
 
        double x, y;
        double angle;
        
        out.println("r");
        while ((fromServer = in.readLine()) != null) {
        	StringTokenizer st = new StringTokenizer(fromServer);
        	x = Double.valueOf(st.nextToken()).doubleValue();
        	y = Double.valueOf(st.nextToken()).doubleValue();
        	angle = Double.valueOf(st.nextToken()).doubleValue();

        	System.out.println();
        	System.out.println("x: " + x + " y: " + y + " angle: " + angle);

        	String fileName = "fcl/FuzzyLogic.fcl";
            FIS fis = FIS.load(fileName,true);
            
            // Error while loading?
            if( fis == null ) { 
                System.err.println("Can't load file: '" + fileName + "'");
                return;
            }
            
            // Set inputs
            fis.setVariable("x", x);
            fis.setVariable("y", y);
            fis.setVariable("angle", angle);

            // Evaluate
            fis.evaluate();

            Variable out_angle = fis.getVariable("out_angle");
            
            // Show output variable's chart
            // JFuzzyChart.get().chart(tip, tip.getDefuzzifier(), true);
        	
			
        	double truckResponse = out_angle.defuzzify();
        	System.out.println("Sending: " + truckResponse);
        	
        	for(Rule r : fis.getFunctionBlock("driver").getFuzzyRuleBlock("No1").getRules()) {
        		if (r.getDegreeOfSupport() > 0) {
        			System.out.println(r);
        		}
        	}
        	
        	// Send wheel action
        	out.println(truckResponse);
        	
            // Request truck position
        	out.println("r");	
        }
 
        out.close();
        in.close();
        stdIn.close();
        kkSocket.close();
    }
}