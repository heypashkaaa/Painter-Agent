import jason.asSyntax.*;
import jason.environment.*;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.util.ArrayList;

// Dziedziczenie Environment Class from Jason framework
public class PainterEnv extends Environment {
    
    private PainterModel model;
    private PainterView  view;
    public static final int GSize = 5; // Grid size
    public static final int EMPTY = 0;
    public static final int AGENT  = 2;
    public static final int OBSTACLE = 4;
    public static final int BRUSH  = 16;
    public static final int KEY    = 32;
    public static final int DOOR   = 64;
    public static final int TABLE  = 128;
    public static final int CHAIR  = 256;
    public static final int COLOR  = 512;
    public static final int CODE   = 1024;
    
    private ArrayList<String> inventory = new ArrayList<>();
    int MAX_CAPACITY = 3; // Max items the agent can carry
    int door_state = 0; // 0 - closed, 1 - opened
    int chair_state = 0; // 0 - unpainted, 1 - painted
    int table_state = 0; // 0 - unpainted, 1 - painted
    double score = 0.0; // Agent's score

    @Override
    //Initializing the environment
    public void init(String[] args) {
  
        model = new PainterModel();
        view  = new PainterView(model);
        model.setView(view);
    
        // Telling agents about the initial percepts
        updatePercepts();
        }

        


    @Override
    // Actions executing block
    public boolean executeAction(String agName, Structure action) {
        boolean result = false;
        double step_cost = 0.0;
        boolean isMovementAction = false;

        if (action.getFunctor().equals("move_up")) {
            isMovementAction = true;
            result = model.move("up"); 
        } 
        else if (action.getFunctor().equals("move_down")) {
            isMovementAction = true;
            result = model.move("down"); 
        } 
        else if (action.getFunctor().equals("move_left")) {
            isMovementAction = true;
            result = model.move("left"); 
        } 
        else if (action.getFunctor().equals("move_right")) {
            isMovementAction = true;
            result = model.move("right"); 
        } 
        else if (action.getFunctor().equals("grab")) {
            Location l = model.getAgPos(0);
            int cell = model.getCellData(l.x, l.y);
            int item = cell & (KEY | CODE | COLOR | BRUSH);

            if (inventory.size() >= MAX_CAPACITY) {
                System.out.println("Inventory full. Cannot grab more items.");
                result = false;
            }
            else {
            if ((cell & KEY) != 0) {
                model.remove(KEY, l); // remove brush from cell
                inventory.add("key");
                result = true;
                System.out.println("Agent grabbed the key " );
            }
            else if ((cell & CODE) != 0) {
                model.remove(CODE, l); // remove code from cell
                inventory.add("code");
                result = true;
                System.out.println("Agent grabbed the code " );
            }
            else if ((cell & COLOR) != 0) {
                model.remove(COLOR, l); // remove color from cell
                inventory.add("color");
                result = true;
                System.out.println("Agent grabbed the color " );
            }
            else if ((cell & BRUSH) != 0) {
                model.remove(BRUSH, l); // remove brush from cell
                inventory.add("brush");    
                result = true;
                System.out.println("Agent grabbed the brush " );
            }
            else {
                result = false;
                System.out.println("Nothing to grab at current location."); 
            }
            }
      
        }
        else if (action.getFunctor().equals("drop")) {
            Location l = model.getAgPos(0);
           
                if (inventory.size() > 0) {
                    String item = inventory.remove(inventory.size() - 1);
                    switch (item) {
                        case "key":
                            model.set(KEY, l.x, l.y);
                            System.out.println("Agent dropped the key ");
                            break;
                        case "code":
                            model.set(CODE, l.x, l.y);
                            System.out.println("Agent dropped the code ");
                            break;
                        case "color":
                            model.set(COLOR, l.x, l.y);
                            System.out.println("Agent dropped the color ");
                            break;
                        case "brush":
                            model.set(BRUSH, l.x, l.y);
                            System.out.println("Agent dropped the brush ");
                            break;
                    }
                    result = true;
                } else {
                    System.out.println("Inventory empty. Nothing to drop.");
                    result = false;
                }
          
            
        }
        else if (action.getFunctor().equals("paint")) {
            Location l = model.getAgPos(0);
            int cell = model.getCellData(l.x, l.y);
            if ((cell & CHAIR) != 0) {
                    if (inventory.contains("brush") && inventory.contains("color")) {
                        System.out.println("Agent painted the chair");
                        chair_state = 1;
                        score += 1.0;
                        result = true;
                    } else {
                        System.out.println("Cannot paint chair. Missing brush or color.");
                        result = false;
                    }
            }
              
             else if ((cell & TABLE) != 0) {
                
                    if (inventory.contains("brush") && inventory.contains("color")) {
                        System.out.println("Agent painted the table");
                        table_state = 1;
                        score += 1.0;
                        result = true;
                    } else {
                        System.out.println("Cannot paint table. Missing brush or color.");
                        result = false;
                    }
                 
            } 
           

        }
        else if (action.getFunctor().equals("open")) {
            if (inventory.contains("key") && inventory.contains("code")) {
                System.out.println("Agent opened the door ");
                door_state = 1;
                score += 0.8;
                result = true;
            } else {
                System.out.println("Cannot open door. Missing key.");
                result = false;
            }
      
        }

        // Points calculation
        // Updating percepts if action was successful (any change of the env)
        if (result) {
            // Only apply movement cost if this is a movement action
            if (isMovementAction) {
                if (inventory.isEmpty()) {
                    step_cost = -0.01; // cost for moving with empty hands
                } else {
                    step_cost = inventory.size() * -0.02; // cost for moving with items
                    
                    // Extra penalty: count each unnecessary item separately
                    // If both table and chair are painted, color and brush are unneeded
                    if (chair_state == 1 && table_state == 1) {
                        if (inventory.contains("color")) step_cost -= 0.01;
                        if (inventory.contains("brush")) step_cost -= 0.01;
                    }
                    
                    // If door is opened, code and key are unneeded
                    if (door_state == 1) {
                        if (inventory.contains("code")) step_cost -= 0.01;
                        if (inventory.contains("key")) step_cost -= 0.01;
                    }
                }
                score += step_cost;
            }
            // grab, drop, paint, open actions don't have a cost
            
            // Print for debugging
            System.out.println("Action: " + action + " | Cost: " + step_cost + " | Total Score: " + String.format("%.3f", score));
            
            updatePercepts();
            try { Thread.sleep(500); } catch (Exception e) {} 
        

        }
        return result;
    }

    
    
    void updatePercepts() {
        clearPercepts(); 
    
        // new coords
        Location l = model.getAgPos(0);
        addPercept(Literal.parseLiteral("at(" + (l.x) + "," + (l.y) + ")"));
    
        // adding the beliefs about objects location
        for (int i = 0; i < GSize; i++) {
            for (int j = 0; j < GSize; j++) {
                int cell = model.getCellData(i, j);
                if ((cell & BRUSH) != 0) addPercept(Literal.parseLiteral("pos(brush," + (i) + "," + (j) + ")"));
                if ((cell & KEY)   != 0) addPercept(Literal.parseLiteral("pos(key,"   + (i) + "," + (j) + ")"));
                if ((cell & DOOR)  != 0) addPercept(Literal.parseLiteral("pos(door,"  + (i) + "," + (j) + ")"));
                if ((cell & TABLE) != 0) addPercept(Literal.parseLiteral("pos(table," + (i) + "," + (j) + ")"));
                if ((cell & CHAIR) != 0) addPercept(Literal.parseLiteral("pos(chair," + (i) + "," + (j) + ")"));
                if ((cell & COLOR) != 0) addPercept(Literal.parseLiteral("pos(color," + (i) + "," + (j) + ")"));
                if ((cell & CODE)  != 0) addPercept(Literal.parseLiteral("pos(code,"  + (i) + "," + (j) + ")"));
                
                // Obstacles
                if ((cell & OBSTACLE) != 0) {
                    addPercept(Literal.parseLiteral("obstacle(" + (i) + "," + (j) + ")"));
                }
            }
        }

        //adding the grabbed objects
        for (String item : inventory) {
            addPercept(Literal.parseLiteral("carrying(" + item + ")"));
        }

        

        // door state
        if (door_state == 1) {
            addPercept(Literal.parseLiteral("door(opened)"));
        } else {
            addPercept(Literal.parseLiteral("door(closed)"));
        }

        // chair state
        if (chair_state == 1) {
            addPercept(Literal.parseLiteral("chair(painted)"));
        } else {
            addPercept(Literal.parseLiteral("chair(unpainted)"));
        }

        // table state
        if (table_state == 1) {
            addPercept(Literal.parseLiteral("table(painted)"));
        } else {
            addPercept(Literal.parseLiteral("table(unpainted)"));
        }

        // current score percept (double value)
        addPercept(Literal.parseLiteral("score(" + score + ")"));

        

    }

    class PainterModel extends GridWorldModel {
        
        public PainterModel() {
            super(GSize, GSize, 1); 
            setAgPos(0, 0, 4);
            
            add(OBSTACLE, 3, 0); 
            add(OBSTACLE, 3, 1); 
            add(OBSTACLE, 1, 3); 
            add(OBSTACLE, 1, 4); 

            add(BRUSH, 0, 0); 
            add(KEY, 0, 1); 
            add(CODE, 2, 0); 
            add(COLOR, 4, 0); 

            add(DOOR, 2, 4); 
            add(CHAIR, 3, 3); 
            add(TABLE, 4, 4); 
        }

        public int getCellData(int x, int y) {
            return data[x][y];
        }

        boolean move(String direction) {
            Location l = getAgPos(0);
            int x = l.x; 
            int y = l.y;
            
           
            if (direction.equals("up") && y > 0) y--;
            else if (direction.equals("down") && y < (GSize - 1)) y++;
            else if (direction.equals("right") && x < (GSize - 1)) x++;
            else if (direction.equals("left") && x > 0) x--;
            else return false;


            if (isFree(OBSTACLE, x, y) && inGrid(x, y)) {
                setAgPos(0, x, y); // Agent moves
                return true;
            }
            return false; 
        }
    }


    class PainterView extends GridWorldView {
        
        Font defaultFont = new Font("Arial", Font.BOLD, 16);
        Font beetleFont  = new Font("Arial", Font.BOLD, 80); // Big Beetle size

        public PainterView(PainterModel model) {
            super(model, "Smart Painter World", 600);
            setVisible(true);
            repaint();
        }
        
        @Override
        public void draw(Graphics g, int x, int y, int object) {
     
            int rx = x * cellSizeW;
            int ry = y * cellSizeH;
            g.setColor(Color.WHITE);
            g.fillRect(rx, ry, cellSizeW, cellSizeH);
            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(rx, ry, cellSizeW, cellSizeH);

           

            if ((object & PainterEnv.OBSTACLE) != 0) {
                g.setColor(Color.BLACK);
                g.fillRect(rx, ry, cellSizeW - 2, cellSizeH - 2);
                return; 
            }

            if ((object & PainterEnv.BRUSH) != 0) drawItem(g, rx, ry, "Brush", Color.PINK);
            if ((object & PainterEnv.KEY)   != 0) drawItem(g, rx, ry, "Key",   Color.ORANGE);
            if ((object & PainterEnv.DOOR)  != 0) drawItem(g, rx, ry, "Door",  Color.MAGENTA);
            if ((object & PainterEnv.TABLE) != 0) drawItem(g, rx, ry, "Table", Color.GRAY);
            if ((object & PainterEnv.CHAIR) != 0) drawItem(g, rx, ry, "Chair", Color.CYAN);
            if ((object & PainterEnv.COLOR) != 0) drawItem(g, rx, ry, "Color", Color.RED);
            if ((object & PainterEnv.CODE)  != 0) drawItem(g, rx, ry, "Code",  Color.GREEN);

           
        }

        @Override
        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
            int rx = x * cellSizeW;
            int ry = y * cellSizeH;
            
            g.setFont(beetleFont);
            g.setColor(Color.BLACK); 
            drawStringCentered(g, "@", rx, ry);
        }

        private void drawItem(Graphics g, int rx, int ry, String label, Color c) {
            g.setFont(defaultFont);
            g.setColor(c);
            g.fillOval(rx + 5, ry + 5, cellSizeW - 10, cellSizeH - 10);
            g.setColor(Color.WHITE);
            drawStringCentered(g, label, rx, ry);
        }

        private void drawStringCentered(Graphics g, String text, int rx, int ry) {
            java.awt.FontMetrics fm = g.getFontMetrics();
            int x = rx + (cellSizeW - fm.stringWidth(text)) / 2;
            int y = ry + (cellSizeH - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(text, x, y);
        }
    }
}
