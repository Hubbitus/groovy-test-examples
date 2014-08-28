import java.util.*;
import java.io.*;
import java.io.PrintWriter;

/**
* http://research.engineering.wustl.edu/~beardj/CtoJava.html
*/
public class StreamTest{


   public StreamTest(){
   }

   /**
   * readStream - reads a stream from specified param stream, then adds it to array
   * @param input_filename String
   * @param length int
   * @return List
   */
   public ArrayList<Data> readStream(int length) throws EOFException,IOException{
      DataInputStream input_stream = new DataInputStream(new BufferedInputStream(System.in,4096));
      ArrayList<Data> array = new ArrayList<Data>();
      Data<Integer> data = new Data<Integer>();
      while(input_stream.available()>=4){
         for(int i=0; i<length; i++){
            data.arr.add(input_stream.readInt());
         }
         array.add(data);
         data = new Data<Integer>();
      }
      return array;
   }



   public static void main(String[] args) throws FileNotFoundException{

      ArrayList<Data> array = null;
      StreamTest t = new StreamTest();
      try{
         array = t.readStream(2);
      }catch(EOFException e){
         System.err.println("We've reached the EOF signal!!");
      }catch(IOException io){
         System.err.println("We've hit an IO Exception!!");
      }finally{
         PrintWriter pw = new PrintWriter("java_output_log.csv");
         for(Data<Integer> d:array){
            pw.write(d.toString());
            pw.write("\n");
         }
         if(!pw.checkError())
            pw.close();
      }
   }


   class Data<T>{
      ArrayList<T> arr;

      public Data(){
         arr = new ArrayList<T>();
      }
      @Override
      public String toString(){
         StringBuilder sb = new StringBuilder();
         for(int i = 0; i<arr.size(); i++){
         sb.append(arr.get(i));
         if(i != arr.size()-1)
            sb.append(",");
         }
         return sb.toString();
         }
   }
}