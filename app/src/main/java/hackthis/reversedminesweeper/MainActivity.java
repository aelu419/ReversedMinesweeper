package hackthis.reversedminesweeper;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;

import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private static final int WIDTH = 10, HEIGHT = 10;
    private static Item[][] map;
    private static final int MINENUM = 30;

    private LinearLayout table;
    private Button start, help, quit;

    private SharedPreferences pref;

    private Date startTime;

    private LinearLayout.LayoutParams rowParams;
    private LinearLayout.LayoutParams buttonParams;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find views
        table = (LinearLayout) findViewById(R.id.map);

        start = (Button) findViewById(R.id.start);
        help = (Button) findViewById(R.id.help);
        quit = (Button) findViewById(R.id.quit);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTime = new Date();
                initialize();
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printHelp();
            }
        });

        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });

        pref = this.getSharedPreferences(this.getPackageName(),MODE_PRIVATE);

        //params related
        rowParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.gravity = Gravity.CENTER_VERTICAL;

        buttonParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

    }

    public void initialize()
    {
        table.removeAllViews();
        //generates new map with
        map = new Item[10][10];
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                map[i][j] = new Item(i, j);
            }
        }
        //place MINENUM mines randomly
        for(int i = 0; i < MINENUM ; i++){
            int x, y;
            do {
                x = (int) (Math.random() * WIDTH);
                y = (int) (Math.random() * HEIGHT);
            } while(map[x][y].isMine);
            map[x][y].isMine = true;
        }
        //initializing call
        refreshMap();

        //initialize visuals
        for(int i = 0; i < HEIGHT; i++){
            LinearLayout temp = new LinearLayout(getApplicationContext());
            temp.setLayoutParams(rowParams);
            temp.setOrientation(LinearLayout.HORIZONTAL);
            for(int j = 0; j < WIDTH; j++){
                temp.addView(map[i][j]);
                System.out.print(map[i][j].isMine+" ");
            }
            System.out.println();
            table.addView(temp);
        }
    }

    public void refreshMap()
    {
        for(int i = 0; i < WIDTH; i++)
        {
            for(int j = 0; j < HEIGHT; j++)
            {
                map[i][j].refresh();
            }
        }

        for(int i = 0; i < WIDTH; i++)
        {
            for(int j = 0; j < HEIGHT; j++)
            {
                if(!(map[i][j].isSelected == map[i][j].isMine)){
                    return;
                }
            }
        }

        //win message
        printMessage();
    }

    public void printMessage()
    {
        Date nowTime = new Date();
        long passed = nowTime.getTime() - startTime.getTime();
        long hour = passed / (1000 * 3600);
        passed = passed % (1000 * 3600);
        long minute = passed / (1000 * 60);
        passed = passed % (1000*60);
        long second = passed / (1000);
        String msg = (hour>0? hour+" hours " : "") + minute+" minutes "+second+" seconds";

        long highHour = pref.getLong(getString(R.string.hour_key), Long.MAX_VALUE);
        long highMinute = pref.getLong(getString(R.string.minute_key), Long.MAX_VALUE);
        long highSecond = pref.getLong(getString(R.string.second_key), Long.MAX_VALUE);

        if(highHour*3600+highMinute*60+highSecond >= hour*3600+minute*60+second){
            msg+="\nYou have broken the record!";

            SharedPreferences.Editor editor = pref.edit();

            editor.putLong(getString(R.string.hour_key),hour);
            editor.putLong(getString(R.string.minute_key),minute);
            editor.putLong(getString(R.string.second_key),second);

            editor.apply();
        }

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setPositiveButton("Play Again", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                initialize();
            }
        }).setNegativeButton("Quit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.exit(0);
            }
        }).setMessage("Good job! You finished the game in "+msg)
                .setCancelable(true).setTitle("Congrats!");
        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }

    public void printHelp()
    {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setPositiveButton("Understood", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        }).setNegativeButton("I don't understand but sure", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        }).setMessage("This game, as its name, is Minesweeper but reversed, like a [in mother Russia mines sweep you] version. In Minesweeper, you click the tiles that are not mines and then see the number. In this game, you know the number and you try to figure out where all the mines are. You win when you place all mines in the right place, when the board would be fully green")
                .setCancelable(true).setTitle("Help");
        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }

    public class Item extends AppCompatButton
    {
        public boolean isSelected;
        public boolean isMine;
        public int x, y;

        public Item(int X, int Y)
        {
            super(getApplication());

            x = X;
            y = Y;

            //layout related
            this.setLayoutParams(buttonParams);
            this.setGravity(Gravity.CENTER);

            //content related
            isMine = false;
            isSelected = false;
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    isSelected = !isSelected;
                    refreshMap();
                }
            });
        }

        public void refresh(){

            int count = 0;
            int actualCount = 0;

            for(int i = x-1; i < x+2; i++)
            {
                for(int j = y-1; j < y+2; j++)
                {
                    if( !( (i < 0 || i > (WIDTH-1)) || (j < 0 || j > (HEIGHT-1)) ) )
                    {
                        if(map[i][j].isSelected)
                        {
                            count++;
                        }
                        if(map[i][j].isMine)
                        {
                            actualCount++;
                        }
                    }
                }
            }

            this.setText(Integer.toString(actualCount));
            if(actualCount == count)
            {
                this.setTextColor(getResources().getColor(R.color.green));
            }
            else {
                this.setTextColor(getResources().getColor(R.color.red));
            }

            if(isSelected)
            {
                this.setBackgroundColor(getResources().getColor(R.color.shaded_background));
            }
            else {
                this.setBackgroundColor(getResources().getColor(R.color.background));
            }
        }

    }
}
