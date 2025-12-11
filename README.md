# Intelligent Agent Painter 🎨 (Jason + Java)

An intelligent BDI (Belief-Desire-Intention) agent implemented using **Jason (AgentSpeak)** and **Java**. The agent navigates a 5x5 grid world to perform tasks (painting furniture, opening doors) while optimizing for the highest possible utility score over 100 randomized episodes.

## 📋 Project Overview

The agent inhabits a grid environment containing obstacles, tools (Brush, Color, Key, Code), and objects to interact with (Table, Chair, Door).

**The Mission:**

1.  **Paint** the Table and Chair (Requires Brush + Color).
2.  **Open** the Door (Requires Key + Code).

**The Challenge:**
The environment is dynamic. In every episode, the locations of the Table, Chair, and Door are randomized. The agent must maximize its score by minimizing movement costs and carrying penalties.

## 🚀 Key Features

  * **Hybrid Architecture:**
      * **Jason (`.asl`):** Handles high-level reasoning, goal selection, and inventory strategy.
      * **Java (Environment):** Handles physics, grid logic, scoring, and complex calculations.
  * \**A* Pathfinding:\*\* The agent uses the A\* algorithm (implemented in Java) to calculate the shortest path around obstacles, ensuring minimal movement costs.
  * **Utility Maximization:** The agent strategically drops tools immediately after a task is completed to avoid the carrying penalty.
  * **Dynamic Adaptation:** The agent re-evaluates its path at every step, allowing it to adapt if the environment changes or if goals shift.
  * **Automated Experiment:** The system runs a loop of **100 episodes**, randomizing the map each time, and calculates the **Average Utility** to benchmark performance.

## 🛠️ Technical Architecture

### 1\. The Brain (`painter.asl`)

The Jason agent follows a strict priority logic to maximize rewards:

1.  **Cleanup:** If a task is done, drop the tools immediately.
2.  **Cluster A (Painting):** High reward (+2.0 total). Prioritized first.
3.  **Cluster B (Opening):** Lower reward (+0.8). Prioritized second.
4.  **Inventory Management:** Checks if tools are missing, finds their coordinates, and fetches them.

### 2\. The Body (`PainterEnv.java`)

The Java environment acts as the physical world:

  * **Perception:** Updates the agent with `pos(Item, X, Y)`, `carrying(Item)`, and `score(S)`, etc.
  * **Action Execution:** Handles `move_towards`, `grab`, `drop`, `paint`, and `open`.
  * **A\* Logic:** A static inner class calculates the optimal `nextStep` towards a target coordinate.
  * **Simulation Loop:** Resets the grid and randomizes furniture positions after every episode.

## 📊 Scoring System

The agent's "Intelligence" is measured by its final Utility Score:

| Action | Score Impact | Notes |
| :--- | :--- | :--- |
| **Paint Table** | `+1.0` | Requires Brush & Color |
| **Paint Chair** | `+1.0` | Requires Brush & Color |
| **Open Door** | `+0.8` | Requires Key & Code |
| **Movement** | `-0.01` | Per step taken |
| **Carrying** | `-0.02` | Per item carried, per step |
| **Carrying unnecessary item** | `-0.03` | Per unnecessary item carried, per step |

**Strategy:** The agent groups tasks. It gathers painting tools, finishes painting, **drops everything**, and only *then* gathers opening tools. This minimizes the "Carrying" penalty.

## 📂 Project Structure

```text
/src
  /agt
     painter.asl        # The AgentSpeak logic (BDI)
  /env
     PainterEnv.java    # The Environment, A* Algorythm, and GUI

painter.mas2j      # Project configuration file
```

## ⚙️ How to Run

1.  Ensure you have **Java** and **Jason** installed.
2.  Open the project files in VSCode.
3.  Type ``` jason painter.mas2j``` in the GitBash terminal.
4.  The GUI will appear, and the agent will begin the 100-episode experiment.
5.  Check the console output for step-by-step logs and the final **Average Utility** calculation.

## 🧩 Code Logic Highlights

**A\* Pathfinding (Java):**

```java
// Calculates the shortest path avoiding obstacles
Location nextStep = AStar.findNextStep((PainterModel)model, current, target);
```

**Strategic Dropping (Jason):**

```asl
// Immediately drop tools when the job is done to save points
+!paint_all : chair(painted) & table(painted) 
   <- !drop_painting_tools. 
```

**Syncing Agent & Environment:**

```asl
// Waits for the Java environment to signal a clean reset
next_episode;
.wait(score(0)); 
!start.
```

