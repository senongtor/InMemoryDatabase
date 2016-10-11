import java.io.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.util.regex.*;

public class InMemoryDB {
    //global variable recording the level of transaction embedding
    private int levelOfTransactions=0;
    private Stack<Transaction> stack;
    private Map<String,Integer> database;
    private Map<Integer,Integer> valueFrequencyCount;
    
    public InMemoryDB(){
        stack=new Stack<Transaction>();
        database=new HashMap<String,Integer>();
        valueFrequencyCount=new HashMap<Integer,Integer>();
    }
    
    class Transaction{
        int level;
        Map<String,Integer> valueHistory;
        public Transaction(int level){
            this.level=level;
            valueHistory=new HashMap<String,Integer>();
        }
        public Transaction(){}
    }
    /**
    @param increment representing whether we want to do increment or decrement operation
    */
    private void updateValCount(Map<Integer,Integer> valueFrequencyCount, int val, boolean increment){
        if(increment){
            if(valueFrequencyCount.containsKey(val)){
                valueFrequencyCount.put(val,valueFrequencyCount.get(val)+1);
            }
            else{
                valueFrequencyCount.put(val,1);
            }
        }
        else{
            if(valueFrequencyCount.containsKey(val)){
                int freq=valueFrequencyCount.get(val);
                if(freq==1){
                    valueFrequencyCount.remove(val);            
                }
                else{
                    valueFrequencyCount.put(val,freq-1);
                }
            }
        }
    }
    
    private void set(String name,int value){
        if(!stack.isEmpty()){
            Transaction lastTransaction=stack.peek();
            //Record original value for each value set during this transaction
            if(!lastTransaction.valueHistory.containsKey(name)){
                if(database.containsKey(name)){
                    lastTransaction.valueHistory.put(name,database.get(name));
                }
                else{
                    //set new added value for -1 so when rollback happens, unset those.
                    lastTransaction.valueHistory.put(name,-1);
                }
            }
        }
        //maintain the correct number of value frequency
        if(database.containsKey(name)){
            updateValCount(valueFrequencyCount,database.get(name),false);
            updateValCount(valueFrequencyCount,value,true);
        }
        else{
            updateValCount(valueFrequencyCount,value,true);
        }
        database.put(name,value);
    }
    private String get(String name){
        StringBuilder sb=new StringBuilder();            
        if(database.containsKey(name)){
            sb.append("> ");
            sb.append(String.valueOf( database.get(name) ));
        }
        else{
            sb.append("> NULL");
        }
        
        return sb.toString();
    }
    private String numEqualto(int value){
        StringBuilder sb=new StringBuilder();
        sb.append("> ");
        if(valueFrequencyCount.containsKey(value)){
            sb.append(String.valueOf(valueFrequencyCount.get(value)));
        }
        else{
            sb.append("0");
        }
        return sb.toString();
    }
    private void unset(String name){
        if(!database.containsKey(name)){
            System.out.println("No record found in the database, no changes made");
            return;
        }
        
        if(!stack.isEmpty()){
            Transaction lastTransaction=stack.peek();
            //Record original value for each value set during this transaction
            if(!lastTransaction.valueHistory.containsKey(name)){            
                lastTransaction.valueHistory.put(name,database.get(name));        
            }
        }
        //maintain the correct number of value frequency      
        updateValCount(valueFrequencyCount,database.get(name),false);
        database.remove(name);     
    }
    
    private void begin(){
        stack.add(new Transaction(levelOfTransactions++));
    }
    
    private void rollBack(){
        
        if(stack.isEmpty()){
            System.out.println("> NO TRANSACTION");
            return;
        }

        Transaction lastTransaction=stack.pop();
        Map<String,Integer> lastTransactionValueHistory=lastTransaction.valueHistory;
        for(String key:lastTransactionValueHistory.keySet()){
            if(lastTransactionValueHistory.get(key)<0){
                unset(key);
            }
            else{
                set(key,lastTransaction.valueHistory.get(key));
            }
        }        
    }
    
    private void commit(){
        if(stack.isEmpty()){
            System.out.println("> NO TRANSACTION");
            return;
        }
        //pop every open transaction off;
        while(!stack.isEmpty()){
            stack.pop();
        }
        
    }
    
    private void printResult(String command){
        String[] tokens=command.split(" +");
        switch(tokens[0].toLowerCase()){
            case "set":
                if(tokens.length!=3){
                    System.out.println("SET command needs to be in the form of SET variableName variableValue");
                    return;
                }
                try{
                    set(tokens[1],Integer.parseInt(tokens[2]));
                }
                catch (NumberFormatException e){
                    System.out.format("Your input %s is not a number. SET can be only applied to numbers",tokens[2]);
                    e.printStackTrace();
                }
                break;
            case "get":
                if(tokens.length!=2){
                    System.out.println("GET command needs to be in the form of GET variableName");
                    return;
                }
                System.out.println(get(tokens[1]));
                break;
            case "unset":
                if(tokens.length!=2){
                    System.out.println("UNSET command needs to be in the form of UNSET variableName");
                    return;
                }
                unset(tokens[1]);
                break;
            case "numequalto":
                if(tokens.length!=2){
                    System.out.format("NUMEQUALTO command needs to be in the form of NUMEQUALTO variableName");
                    return;
                }
                try{
                    System.out.println(numEqualto(Integer.parseInt(tokens[1])));
                }
                catch (NumberFormatException e){
                    System.out.format("Your input %s is not a number. NUMEQUALTO can be only applied to numbers",tokens[1]);
                    e.printStackTrace();
                }
                break;
            case "end": break;
            case "begin":
                if(tokens.length!=1){
                    System.out.println("BEGIN command doesn't take any input");
                    return;
                }
                begin();
                break;
            case "rollback":
                if(tokens.length!=1){
                    System.out.println("ROLLBACK command doesn't take any input");
                    return;
                }
                rollBack();
                break;
            case "commit":
                if(tokens.length!=1){
                    System.out.println("COMMIT command doesn't take any input");
                    return;
                }
                commit();
                break;
            default: System.out.println("No corresponding command found"); return;
        }
    }
    public static void main(String args[] ) throws Exception {
        InMemoryDB myDB=new InMemoryDB();
        Scanner in = new Scanner(System.in);
        while(in.hasNextLine()){
            String currLine=in.nextLine();
            System.out.println(currLine);
            myDB.printResult(currLine);    
        }  
        in.close();
    }
}
