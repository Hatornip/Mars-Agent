# Multi-Agent System Simulation: Resource Collection in an Unknown and Hostile Environment

This repository implements a simulation of a multi-agent system (MAS) designed to collect rare stones in an unknown and hostile planetary environment. The agents are autonomous robots tasked with exploring a terrain without prior maps, navigating obstacles, and returning to a mother ship via a global guidance signal.

The simulation is built using the **JADE** (Java Agent DEvelopment Framework) and follows a **BDI (Beliefs, Desires, Intentions)** architecture to model agent intelligence. It demonstrates emergent collective behaviors through **stigmergy** (indirect communication via environmental cues) and includes scenarios that simulate real-world constraints such as limited energy, environmental hazards, and dynamic changes.

---

## 🚀 Installation & Setup Guide

### ✅ Prerequisites

- **Java Development Kit (JDK)**: Version 8 or higher is required.
- **JADE Framework**: The only external dependency.

### 🔧 How to Install JADE

1. Download the latest JADE distribution (e.g., `JADE-bin-4.5.0.zip`) from the official site:  
   [https://jade.tilab.com/](https://jade.tilab.com/)

2. Extract the archive to your local machine.

3. Locate the `jade.jar` file inside the `lib/` folder of the extracted JADE package.

4. Copy `jade.jar` into your project’s `jade/lib/` directory.

---

### 📁 Project Structure

Ensure your project follows this directory structure:
projet-jade/  
├── src/  
│   └── sma/  
│       ├── Main.java  
│       └── ... (all other Java files)  
│  
├── jade/  
│   └── lib/  
│       └── jade.jar  
│  
├── bin/  
│  
├── compile.sh   (Linux)  
├── compile.bat  (Windows)  
└── run.sh       (Linux)  
└── run.bat      (Windows)  


> ✅ This structure is required for scripts to work properly.

---

### 🔍 Step 1: Compile the Project

Open a terminal or command prompt in the root directory of your project (`projet-jade/`).

#### On Linux/(Not tested on MacOS):
```bash
sh compile.sh
```

#### On Windows:
```bash
sh compile.bat
```

### 🚀 Step 2: Run the Simulation

Launch the application using:

#### On Linux/(Not tested on MacOS):
```bash
sh run.sh
```

#### On Windows:
```bash
sh run.bat
```



A configuration window will appear where you can set simulation parameters:

Scenario type
Number of agents
Resource quantity
Click "Launch Simulation" to begin.
