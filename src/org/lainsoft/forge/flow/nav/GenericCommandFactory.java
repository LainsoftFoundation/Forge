/**
 * GenericCommandFactory.java is part of Forge Project.
 *
 * Copyright 2004,2005 LainSoft Foundation, Demetrio Cruz
 *
 * You may distribute under the terms of either the GNU General Public
 * License or the Artistic License, as specified in the README file.
 *
*/
package org.lainsoft.forge.flow.nav;

import org.lainsoft.forge.flow.helper.ViewHelper;
import org.lainsoft.forge.flow.nav.config.data.RoutingCommands;
import org.lainsoft.forge.flow.nav.config.data.Route;
import org.lainsoft.forge.flow.nav.config.reader.RoutingCommandsReader;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Generic implementation of a command factory, it coverts the path of a stimulus
 * into a cannonical class name to be load.
 * @author Copyright 2004, 2005 <a href="http://www.lainsoftfoundation.org">LainSoft Foundation</a>, Demetrio Cruz
 */
public class GenericCommandFactory
    implements CommandFactory{

    private static Log log = LogFactory.getLog(GenericCommandFactory.class);
    
    private static Map internalCache;
    private static CommandNotFoundCommand cnfc;
    private static String fileName="";

    private GenericCommandFactory (Map internalCache){
        if (this.internalCache==null)
            this.internalCache = internalCache;
        else
            this.internalCache.putAll(internalCache);
    }
    
    /**
     * Creates a new Instance of <code>GenericCommandFactory</code>.
     */
    public static GenericCommandFactory 
    newInstance(){
        return new GenericCommandFactory(new HashMap());
    }
    
    /**
     * Creates a new Instance of <code>GenericCommandFactory</code>.
     * @param cache an inital cache.
     */
    public static GenericCommandFactory
    newInstance(Map cache){
        return new GenericCommandFactory (cache);
    }
    
    /**
     * Retrives a <code>Command</code> based on an stimulus, that is 
     * the cannonical class name of a command in the form of:<br>
     * <code>/some/package/ClassName</code>.
     * @param stimulus the stimulus to be load as a command.
     * @param helper The ViewHelper to be used by the command.
     */
    public Command
    getCommand (String stimulus, ViewHelper helper){
        if (stimulus == null || stimulus.trim().equals("") || stimulus.trim().equals("/")){
            return new CommandNotFoundCommand();
        }

        try {
            stimulus=stimulus.replaceAll("/",".");
            log.debug("Loading>"+stimulus);
            
            if (internalCache.containsKey(stimulus)){
                return (Command)((Class)internalCache.get(stimulus)).newInstance();
            }
            
            Class tempCommand = null;
            internalCache.put(stimulus,
                              (tempCommand = Class.forName (stimulus)));
            return (Command) tempCommand.newInstance();
        }catch (ClassNotFoundException cnfe){
            if (cnfc == null)
                cnfc = new CommandNotFoundCommand();
            return cnfc;                        
        }catch (InstantiationException ie){
            ie.printStackTrace();
        }catch (IllegalAccessException iae){
            iae.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    
    public List
    getCompositesMembers(String classname, ViewHelper helper){
        RoutingCommands routingCommands = getRoutingCommands(helper);
        List routes = routingCommands.getRoutes();
        for (int i=0; i<routes.size(); i++){
            if (((Route)routes.get(i)).contains(classname))
                return ((Route)routes.get(i)).getComposite().getMembers();
        }
        return new ArrayList();
    }



    private Command
    getComposite(String stimulus, ViewHelper helper){
        
        RoutingCommands routingCommands = getRoutingCommands(helper);
        List routes = routingCommands.getRoutes();
        Route route = new Route();
        for (int i=0; i<routes.size(); i++){
            if(((Route)routes.get(i)).matchesRoute(stimulus))
                route = (Route)routes.get(i);
        }
        return null;
    }



    private RoutingCommands
    getRoutingCommands(ViewHelper helper){
        if (fileName.trim().equals(""))
            fileName = ((fileName = 
                         helper.getApplicationContext().getInitParameter("zone_configuration_file"))==null
                        ? ""
                        : fileName).matches("((file)|(http)):/.+") ? fileName
                : helper.getApplicationContext().getRealPath(fileName);
        return RoutingCommandsReader.getInstance().read (fileName);
    }

}
