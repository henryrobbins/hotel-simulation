set ROOMS;
set GUESTS;
set HOUSEKEEPERS;
set TIME ordered;

param type {ROOMS} integer > 0;
param release {ROOMS} integer >= 0;
param process {ROOMS} integer > 0;
param deadline {ROOMS} default last(TIME);

param request {GUESTS} integer > 0;
param arrival {GUESTS} integer >= 0;

param weight {GUESTS, ROOMS} >= 0;

var schedule {r in ROOMS, h in HOUSEKEEPERS, t in TIME} integer >=0, <= 1;
var completion {r in ROOMS} integer >= 0;
var makespan integer >= 0;
var tardiness {r in ROOMS} integer >= 0;
var maxTardiness integer >= 0;

minimize Makespan: makespan;
minimize Sum_Completion_Time: sum {r in ROOMS} completion[r];
minimize Sum_Tardiness: sum {r in ROOMS} tardiness[r];
minimize Max_Tardiness: maxTardiness;

subject to Release_Time {r in ROOMS}:
	release[r]+1 <= sum {h in HOUSEKEEPERS, t in TIME} t*schedule[r,h,t];

subject to Every_Room_Cleaned {r in ROOMS}:
	sum {h in HOUSEKEEPERS, t in TIME} schedule[r,h,t]= 1;
	
subject to Clean_Time {r in ROOMS}:
	completion[r]= sum {h in HOUSEKEEPERS, t in TIME} (t*schedule[r,h,t]) + process[r] - 1;

subject to One_Job_At_A_Time {h in HOUSEKEEPERS, t in TIME}:
	sum {r in ROOMS, pt in TIME: pt < t && pt >= max(0,t-process[r])} schedule[r,h,pt] <= 1;
	
subject to Max_Completion_Time {r in ROOMS}:
    completion[r] <= makespan;
    
subject to Waiting_Time {r in ROOMS}:
	tardiness[r] >= completion[r] - deadline[r] + 1;

subject to Tardiness {r in ROOMS}:
	tardiness[r] >= 0;
	
subject to Max_Waiting_Time {r in ROOMS}:
	tardiness[r] <= maxTardiness;