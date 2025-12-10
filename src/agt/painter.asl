
// LOGICAL RULES 
ready_to_paint :- carrying(brush) & carrying(color).
ready_to_open  :- carrying(key) & carrying(code).


//  GOAL
!start.
+!start <- .print("I am starting the task!");
            !paint_table;
            !paint_chair;
            drop;drop;
            !open_door;
            ?score(S);
            .print("I finished all the tasks! Here is my score: ", S).

// PLANS FOR ACHIEVING SUBGOALS

+!paint_table : ready_to_paint <- ?pos(table,X,Y); !goto(X,Y); paint.
+!paint_table : not ready_to_paint <- !get(brush); !get(color); !paint_table.

+!paint_chair : ready_to_paint <- ?pos(chair,X,Y); !goto(X,Y); paint.
+!paint_chair : not ready_to_paint <- !get(brush); !get(color); !paint_chair.

+!open_door : ready_to_open <- ?pos(door,X,Y); !goto(X,Y); open.
+!open_door : not ready_to_open <- !get(key); !get(code); !open_door.

// SMALLER TASKS 

+!get(Item) : carrying(Item) <- .print("I alreadY have it!").
+!get(Item) : not carrying(Item) <- ?pos(Item, X,Y); !goto(X,Y); grab.

+!goto(X,Y) : at(X,Y) <- .print("I'm alreadY here!").
-!goto(X,Y) <- .print("Cannot reach ", X, ",", Y); .fail.

+!goto(X,Y) : at(AgX,AgY) & AgX < X <- move_right; !goto(X,Y).
+!goto(X,Y) : at(AgX,AgY) & AgX > X <- move_left; !goto(X,Y).
+!goto(X,Y) : at(AgX,AgY) & AgY < Y <- move_down; !goto(X,Y).
+!goto(X,Y) : at(AgX,AgY) & AgY > Y <- move_up; !goto(X,Y).