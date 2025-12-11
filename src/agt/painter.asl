// --- KNOWLEDGE BASE ---
painting_tool(brush).
painting_tool(color).
opening_tool(key).
opening_tool(code).

has_painting_tools :- carrying(brush) & carrying(color).
has_opening_tools  :- carrying(key) & carrying(code).

// --- INITIAL GOAL ---
!start.

+!start 
    : experiment_over 
    <- .print("100 Episodes Complete.").

+!start : not experiment_over
    <- .print("--- STARTING THE EPISODE ---");
       // Priority 1: Higher Reward(Painting = +2.0)
       !paint_all;
       // Priority 2: Lower Reward(Door = +0.8)
       !open_all;
       ?score(S);
       .print("--- ALL THE TASKS ARE FINISHED. FINAL SCORE: ", S);
       next_episode;
       .wait(score(0));
       !start.

// --- PAINTING ---

// If both are painted, ensure we drop tools to save points
+!paint_all 
    : chair(painted) & table(painted) 
    <- .print("Painting objectives complete.");
       !drop_painting_tools. 

// Execution: If not done, get tools and do the job
+!paint_all 
    <- .print("Objective: PAINTING");
       !prepare_tools(painting); 
       !paint_table;
       !paint_chair;
       !paint_all.

// --- OPENING ---


+!open_all 
    : door(opened) 
    <- .print("Opening objectives complete.");
       !drop_opening_tools. 

+!open_all 
    <- .print("Objective: OPENING");
       !prepare_tools(opening); 
       !open_door;
       !open_all.

// --- PLANS ---

// Table 
+!paint_table : table(painted) <- .print("Table is painted.").
+!paint_table : has_painting_tools
    <- ?pos(table,X,Y); 
       !goto(X,Y); 
       paint; 
       .print("Action: Painted the table.").
+!paint_table <- !prepare_tools(painting); !paint_table.

// Chair 
+!paint_chair : chair(painted) <- .print("Chair is painted.").
+!paint_chair : has_painting_tools
    <- ?pos(chair,X,Y); 
       !goto(X,Y); 
       paint; 
       .print("Action: Painted the chair.").
+!paint_chair <- !prepare_tools(painting); !paint_chair.

// Door 
+!open_door : door(opened) <- .print("Door is open.").
+!open_door : has_opening_tools
    <- ?pos(door,X,Y); 
       !goto(X,Y); 
       open; 
       .print("Action: Opened the door.").
+!open_door <- !prepare_tools(opening); !open_door.

// --- BACKPACK MANAGEMENT ---

// Fetching Logic
+!prepare_tools(painting) <- !ensure_has(brush); !ensure_has(color).
+!prepare_tools(opening)  <- !ensure_has(key); !ensure_has(code).

+!ensure_has(Item) : carrying(Item).
+!ensure_has(Item) : not carrying(Item)
    <- .print("Need ", Item, ". Fetching...");
        .wait(pos(Item, _, _));
       ?pos(Item, X, Y);
       !goto(X,Y);
       !try_grab(Item).

// Grabbing with Error Handling
+!try_grab(Item) 
    <- grab; 
       .print("Success: Grabbed ", Item).
-!try_grab(Item) // Failure recovery (Inventory Full)
    <- .print("Inventory Full! Making space for ", Item);
       !make_space_for(Item);
       !try_grab(Item).

// Drops items specifically to stop the -0.02/step penalty

+!drop_painting_tools
    <- if (carrying(brush)) { drop(brush); .print("Dropped Brush (Cleanup)"); };
       if (carrying(color)) { drop(color); .print("Dropped Color (Cleanup)"); }.

+!drop_opening_tools
    <- if (carrying(key)) { drop(key); .print("Dropped Key (Cleanup)"); };
       if (carrying(code)) { drop(code); .print("Dropped Code (Cleanup)"); }.

// Emergency Drop (Inventory Full)
+!make_space_for(NeededItem)
    : carrying(Useless) & Useless \== NeededItem
    <- .print("Dropping ", Useless, " to make room.");
       drop(Useless).

// --- MOVEMENT LOGIC (Java A*) ---

// Lucky Case: We are already there
+!goto(X,Y) : at(X,Y) 
    <- .print("Arrived at destination (", X, ",", Y, ").").

// Recursive walking: Move one step towards target
+!goto(X,Y) : not at(X,Y)
    <- move_towards(X, Y); 
       !goto(X,Y).         
       
// Failure 
-!goto(X,Y) 
    <- .print("Path blocked or target unreachable. Waiting...");
       .wait(500);
       !goto(X,Y).