package saarland.cispa.se.tribble.execution.componentExtraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Paths;

/***
* concrete implementation of a ComponentsBuilder for the livingstandard-url grammar producing a component representation 
  not specialized on a particular input format
  other component builders use this class to create their representations whenever possible to avoid code duplications

*/
public class UniversalURLComponentsBuilder extends UniversalComponentsBuilder {

	ArrayList<String> InternalComponentNames=new ArrayList<String>();
    HashMap<String, String> translation=new HashMap<>();
    String format = "basicComponents";
    URLComponentsUtil util=new URLComponentsUtil();
    HashMap<String, String> components=new HashMap<>();

    HashMap<String, String> basecomponents=new HashMap<>();
    HashMap<String, String> relcomponents=new HashMap<>();
    int max_dict_entries=5;

    public UniversalURLComponentsBuilder(){
    	super();
    	this.InternalComponentNames.add("scheme");
    	this.InternalComponentNames.add("host");
    	this.InternalComponentNames.add("port");
    	this.InternalComponentNames.add("path");
    	this.InternalComponentNames.add("query");
    	this.InternalComponentNames.add("fragment");
    	
    	this.InternalComponentNames.add("input"); 

    	


    }

    @Override
    public void addComponent(String name, String content){
        // on the URL grammar specialized: handling duplicates
        addAndKeepOldEntry(name, 0, content);
    }

    private void addAndKeepOldEntry(String name,int id, String content){
        if(id>=max_dict_entries){
            // relevant rules are not present that often, and we are not interested 
            // in keeping track of single digits or characters 
            return; 
        }
        //check for other entries for the same rule
        String suffix=(id!=0 ? String.valueOf(id) : "");
        String oldcontent=dict.get(name+suffix);
        if(oldcontent!=null ){
            if(! oldcontent.equals(content)){
                //save old entry with smaller id
                dict.put(name+String.valueOf(id), oldcontent);
                //remove old entry but keep the original name as key: content of rule and rule0 should be identical
                if(id!=0){
                    dict.remove(name+suffix);
                }
                // try to place the new entry with a higher id
                id++;
                addAndKeepOldEntry(name, id, content);
                return;
            }
            else{
                //no need to save the same content more than once
                return;
            }
            
        }
        else{
            dict.put(name+suffix, content);
        }
        return;
    }
    

    /***
    * creates a dictonary of basic component names and component contents which can be accessed by
    * more specialized component builders
    */
    public void prepareComponents(){ 
    	//determine which method to use
        if(dict.get("baseAndRelativeURL")!=null){
            // there are base and/or relative URLs present
            prepareBaseComponents();
            prepareRelativeComponents();
            combineBaseAndRelativeComponents();

            System.out.println(components);
            if(components.get("relative_host").equals("NOHOSTCANDIDATE")){
                System.out.println(dict);
            }
        }
        else{
            // there is only a absolute URL present
            prepareBasicComponents();
        }
        
    	return;
    }


    /***
    *  method to access components outside the basic prepared components,
    *  utilizes an additional string to select the correct instantiation in case a rule is used more than once
    *  note1: at most max_dict_entries are present
    *  note2: using a high level rule to access components is preferable
    *
    * @param grammarrule the rule whose instantiation is requested
    * @param containedIn a string which contains the desired content (returned content is a substring of containedIn)
    * @return the instantiation of the specified rule
    */
    public String getSpecialComponentContent(String grammarrule, String containedIn){  //TODO
        ArrayList<String> candidatekeys=getAllCandidates(grammarrule);
        if(containedIn != null && !(containedIn.equals(""))){
            //collect all candidate contents and compare them to containedIn 
            for(String ck : candidatekeys){
                String content=dict.get(ck);
                if(containedIn.contains(content)){
                    return content;
                }
            }

        } 
        //containedIn is empty or none of the candidates match
    	return null;
    }

    private ArrayList<String> getAllCandidates(String grammarrule){
        ArrayList<String> candidatekeys=new ArrayList<String>();
        for(String key :dict.keySet()){
            if(key.startsWith(grammarrule+"[0-9]") || key.equals(grammarrule)){
                candidatekeys.add(key);
            }
        }
        return candidatekeys;
    }


    /***
    * prepare the components of the produced base URL
    * note: normalization and formatting are applied when combining base and relative URL
    ***/
    private void prepareBaseComponents(){ //TODO enforce lowercase?
        //get full base URL
        String base="";
        boolean specialbase=false;
        boolean filebase=false;
        String tmp=dict.get("specialBaseURL");
        if(tmp != null){
            base=tmp;
            specialbase=true;
        }
        else{
            tmp=dict.get("fileBaseURL");
            if(tmp != null){
                base=tmp;
                filebase=true;
            }
            else{
                base=dict.get("otherBaseURL");
            }
        }
        components.put("base", base);
        // collect base components
        String bscheme="";
        if(specialbase){
            bscheme=getSpecialComponentContent("URLspecialSchemeNotFile", base);
        }
        else{
            if(filebase){
                bscheme=getSpecialComponentContent("URLschemeFile", base);
            }
            else{
                bscheme=getSpecialComponentContent("URLnonSpecialScheme", base);
            }
        }
        components.put("base_scheme", bscheme);
        String bhost=prepareHost(base);
        if(bhost != null){
            components.put("base_host", bhost);
        }
        
        String bp=getSpecialComponentContent("URLport", base); //TODO port digits could be in ip
        if(bp!=null){
            components.put("base_port", bp);
        }
        String bpa=preparePath(base);
        String dl=getSpecialComponentContent("windowsDriveLetter", bpa);

        if(specialbase || filebase){
            components.put("base_path", util.normalizePath(bpa, dl)); //TODO wait for combining before normalization?
        }
        else{
            components.put("base_path", bpa);
        }
        
        String bq=null;
        if(specialbase || filebase){
            bq=getSpecialComponentContent("URLSpecialquery", base);
        }
        else{
            bq=getSpecialComponentContent("URLquery", base);
        }
        if(bq != null){
            components.put("base_query", bq);
        }
        
        String bf=getSpecialComponentContent("URLfragment", base);
        if(bf!=null){
            components.put("base_fragment", bf);
        }

        return;
    }

    /***
    * dual to prepareBaseComponents; prepare the components of the relative URL
    ***/
    private void prepareRelativeComponents(){
        String bar=dict.get("baseAndRelativeURL");
        String rel;
        boolean woscheme=true;
        rel=dict.get("absoluteURLwithFragment"); //base is special/file/other/absolute
        if(rel!=null){
            woscheme=false;
        }
        else{
            String sprel=dict.get("specialRelativeURL");
            String firel=dict.get("fileRelativeURL");
            String otrel=dict.get("otherRelativeURL");
            for(String r: Arrays.asList(sprel, firel, otrel)){
                if(r!=null){
                    rel=r;
                }
            }
        }
        components.put("relative", rel);
        // prepare scheme
        //if(!woscheme){
            String rscheme=null;
            String sp=getSpecialComponentContent("URLspecialSchemeNotFile", rel);
            String fi=getSpecialComponentContent("URLschemeFile", rel);
            String ot=getSpecialComponentContent("URLnonSpecialScheme", rel);
            for(String s: Arrays.asList(sp, fi, ot)){
                if(s != null){
                    rscheme=s;
                }
            }
            components.put("relative_scheme", rscheme);
        //}
        // prepare host
        String rhost=prepareHost(rel);
        if(rhost != null){
            components.put("relative_host", rhost);
        }
        //prepare path
        String rpath=preparePath(rel); //TODO make preparePath stronger
        if(rpath != null){
            components.put("relative_path", rpath);
        }
        // prepare query
        String rq=null;
        String rsq=getSpecialComponentContent("URLSpecialquery", rel);
        String rnsq=getSpecialComponentContent("URLquery", rel);
        if( rsq != null){
            rq=rsq;
        }
        else{
            if( rnsq != null){
                rq=rnsq;
            }
        }
        if(rq != null){
            components.put("relative_query", rq);
        }
        // prepare fragment
        String rf=getSpecialComponentContent("URLfragment", rel);
        if(rf != null){
            components.put("relative_fragment", rf);
        }


        return;
    }

    /***
    * utilizes the prepared components of base and relative URL to create the final component representation
    ***/
    private void combineBaseAndRelativeComponents(){
        components.put("input", dict.get("baseAndRelativeURL"));
        return;
    }

    private String prepareQuery(String parent){
        String rq=null;
        String sq=getSpecialComponentContent("URLSpecialquery", parent);
        String nsq=getSpecialComponentContent("URLquery", parent);
        if( sq != null){
            rq=sq;
        }
        else{
            if( nsq != null){
                rq=nsq;
            }
        }
        if(parent.contains("\\?"+rq)){
            return rq;
        }
        //TODO may need to check for other query entries
        return null;
    }

    private String prepareHost(String parent){//TODO make sure that the returned host is not a real substring of the actual host
        String host=null;
        String ophost=getSpecialComponentContent("opaqueHost", parent);
        String d=getSpecialComponentContent("domain", parent); 
        String ipv4=getSpecialComponentContent("ipv4address", parent);
        String ip=getSpecialComponentContent("ipv6address", parent);

        String originalip=ip;

        if(d != null){
            host=d;
        }
        else{
            if(ip != null){
                host="["+util.formatIPv6(ip)+"]";
            }
            else{
                if(ipv4 != null){
                    host=ipv4;
                }
                else{
                    host=ophost;
                }
            }
        }
        // make sure this is the full host and not a substring of it
        for(String ending: Arrays.asList("/", ":", "?", "#")){
            if(parent.contains("//"+host+ending) || parent.contains("//"+originalip+ending)){
                return host;
            }
        }
        if(parent.endsWith("//"+host) || parent.endsWith("//"+originalip)){
            return host;
        }

        
        // overlapping hosts, need some extra work
        host="XXX";
        return host;
    }

    private String preparePath(String parent){ //TODO check if these are enough
        String path=null;
        String pa=getSpecialComponentContent("pathAbsoluteURL", parent);
        String panW=getSpecialComponentContent("pathAbsoluteNonWindowsFileURL", parent);
        String prsl=getSpecialComponentContent("pathRelativeSchemelessURL", parent);
        
        for(String p:Arrays.asList(pa, panW, prsl)){
            if(p!=null){
                path=p;
            }
        }
        return path;

    }


    /***
    * prepares the components of an absolute URL without base
    ***/
    private void prepareBasicComponents(){
        // populate this.components from raw dict and apply necessary transformations i.e. ipv6 formatting, 
        // path formatting, ...

        //components which need no further processing
        components.put("port", dict.get("URLport")); 
        components.put("fragment", dict.get("URLfragment"));
        components.put("input", dict.get("absoluteURLwithFragment")); 


        // prepare scheme
        String specialnf=dict.get("URLspecialSchemeNotFile");
        String nonspecial=dict.get("URLnonSpecialScheme");
        String file=dict.get("URLschemeFile");

        for (String content: Arrays.asList(specialnf, nonspecial, file)){
          if(content !=null){
            components.put("scheme", content.toLowerCase());
          }
        }

        // prepare host
        String reshost=prepareHost(components.get("input"));
        if(reshost != null){
            components.put("host", reshost);
        }
        

        // prepare path
        String pa=dict.get("pathAbsoluteURL");
        String panW=dict.get("pathAbsoluteNonWindowsFileURL");
        String prsl=dict.get("pathRelativeSchemelessURL");
           // include all leading slashes in non special urls without host
           String rel=dict.get("schemeRelativeURL"); 
           String ohp=dict.get("opaqueHostAndPort");
           if(nonspecial!=null){
               if(rel!=null){
                   if(ohp !=null){
                       pa=(! ohp.equals("") ? pa : rel);
                   }
                   
               }
       
           }
        String pathcontent="";
           String driveletter=dict.get("windowsDriveLetter");
        /*if (panW != null){
          pa=null; //pathAbsoluteNonWindowsFileURL contains pathAbsoluteURL
        }*/
        for (String content: Arrays.asList(panW, pa, prsl)){
          if(content !=null){ // only normalize path if a special scheme is used
            components.put("path", (nonspecial !=null ? content : util.normalizePath(content, driveletter)));  
          }
        }

        // prepare query
        String query;
        String qs=dict.get("URLSpecialquery");
        String qns=dict.get("URLquery");

        if(qs != null){
          query=qs; 
        }
        else{
          query=qns; 
        }
        components.put("query", query);
        
    }

}