import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class test {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        ArrayList<String> output = new ArrayList<>();
        String[] words = input.split(" ");
        for(int i = 0 ;i< words.length;i++){
            String word = words[i];
            output.add(swap(word));
        }
        for(int i = 0 ; i<output.size();i++){
            System.out.print(output.get(i) + " ");
        }
    }

    static String  swap(String word){
        char[] ch = word.toCharArray();
        char[] out = new char[ch.length];
        for(int i = ch.length-1;i>=0;i--){
            out [ch.length-i-1] = ch[i];
        }
        String res = String.valueOf(out);
        return res;
    }
}