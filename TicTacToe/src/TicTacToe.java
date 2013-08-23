import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
public class TicTacToe extends JFrame {
    public static void main(String[]args){new TicTacToe();}
    public TicTacToe(){
        super("Tic Tac Toe");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3,3));
        for(int r=0;r<3;r++) {
            for(int c=0;c<3;c++) {
                final JButton b=new JButton();
                b.setBackground(Color.WHITE);
                b.setActionCommand(r+","+c);
                b.addActionListener(new ActionListener(){
                    @Override public void actionPerformed(ActionEvent e){
                        b.setText(turn()==0?"X":"O");
                        b.setEnabled(false);
                        String[]s=e.getActionCommand().split(",");
                        at(Integer.parseInt(s[0]),Integer.parseInt(s[1]),turn());
                        String title;
                        switch(win()){
                        case 0:title="X Wins!";break;
                        case 1:title="O Wins!";break;
                        case 2:title="Draw!";break;
                        default:return;
                        }
                        if(JOptionPane.showConfirmDialog(TicTacToe.this,"Again?",title,JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)System.exit(0);
                        dispose();
                        new TicTacToe();
                    }
                });
                add(b);
            }
        }
        setSize(250,250);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    private int ttt=0;
    private static int[]wmx={86016,1344,21,66576,16644,4161,65793,4368,172032,2688,42,133152,33288,8322,131586,8736};
    public int win(){
        for(int m:wmx)if((ttt&m)==m)return turn()^1;
        for(int i=0;i<18;i+=2)if(get(i,2)==0)return-1;
        return 2;
    }
    public int at(int r,int c){return get(r*6+(c<<1),2);}
    public void at(int r,int c,int v){set(r*6+(c<<1),v+1);ttt^=1<<18;}
    public int turn(){return (ttt>>18)&1;}
    private int get(int pos,int len){return(ttt>>pos)&(2147483647>>(31-len));}
    private void set(int pos,int v){ttt|=v<<pos;}
}