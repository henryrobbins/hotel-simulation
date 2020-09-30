set ROOMS;
set GUESTS;
set HOUSEKEEPERS;
set TIME ordered;

param type {ROOMS} integer > 0;
param release {ROOMS} integer >= 0;
param process {ROOMS} integer > 0;

param request {GUESTS} integer > 0;
param arrival {GUESTS} integer >= 0;

param weight {GUESTS,ROOMS} >= 0;
param minMeanMatchingWeight default 0;

var schedule {r in ROOMS, h in HOUSEKEEPERS, t in TIME} integer >=0, <= 1;
var assign {g in GUESTS, r in ROOMS} integer >= 0, <= 1;
var completion {r in ROOMS} integer >= 0;
var makespan integer >= 0;
var tardiness {g in GUESTS} integer >= 0;
var maxTardiness integer >= 0;

minimize Sum_Tardiness: sum {g in GUESTS} tardiness[g];
minimize Max_Tardiness: maxTardiness;
maximize Mean_Satisfaction: sum {g in GUESTS, r in ROOMS} assign[r,g]*weight[g,r];
maximize Mean_Satisfaction_And_Sum_Tardiness: sum {g in GUESTS, r in ROOMS} assign[g,r]*weight[g,r] - (sum {g in GUESTS} tardiness[g]);

# HOUSEKEEPING CONSTRAINTS

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
	
# ROOM ASSIGNMENT CONSTRAINTS

subject to Accommodate_Everyone {g in GUESTS}:
	sum {r in ROOMS} assign[g,r] = 1;
	
subject to Room_Capacity {r in ROOMS}:
	sum {g in GUESTS} assign[g,r] <= 1;
	
subject to Requested_Type {g in GUESTS}: 
	sum {r in ROOMS} assign[g,r]*type[r] >= request[g]; 
    
subject to Average_Satisfaction:
    (sum {g in GUESTS, r in ROOMS} assign[g,r]*weight[g,r])/card(GUESTS) >= minMeanMatchingWeight;
	
# OVERLAP
	
subject to Waiting_Time {g in GUESTS}:
	tardiness[g] >= (sum {r in ROOMS} assign[g,r]*completion[r]) - arrival[g] + 1;

subject to Tardiness {g in GUESTS}:
	tardiness[g] >= 0;
	
subject to Max_Waiting_Time {g in GUESTS}:
	tardiness[g] <= maxTardiness;