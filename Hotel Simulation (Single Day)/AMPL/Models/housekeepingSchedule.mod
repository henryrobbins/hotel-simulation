set ROOMS;
set GUESTS;
set HOUSEKEEPERS;
set TIME ordered;

param roomType {ROOMS} integer > 0;
param checkout {ROOMS} integer >= 0;
param cleanTime {ROOMS} integer > 0;
param deadline {ROOMS} default last(TIME);

param requestType {GUESTS} integer > 0;
param checkin {GUESTS} integer >= 0;

param preferences {ROOMS,GUESTS} integer >= 0;
param satisfaction {ROOMS,GUESTS} >= 0;

var schedule {r in ROOMS, h in HOUSEKEEPERS, t in TIME} integer >=0, <= 1;
var completion {r in ROOMS} integer >= 0;
var maxCompletion integer >= 0;
var overlap {r in ROOMS} integer >= 0;
var maxOverlap integer >= 0;

minimize Makespan: maxCompletion;
minimize Sum_Completion_Time: sum {r in ROOMS} completion[r];
minimize Mean_Wait_Time: sum {r in ROOMS} overlap[r];
minimize Max_Wait_Time: maxOverlap;

subject to Release_Time {r in ROOMS}:
	checkout[r]+1 <= sum {h in HOUSEKEEPERS, t in TIME} t*schedule[r,h,t];

subject to Every_Room_Cleaned {r in ROOMS}:
	sum {h in HOUSEKEEPERS, t in TIME} schedule[r,h,t]= 1;
	
subject to Clean_Time {r in ROOMS}:
	completion[r]= sum {h in HOUSEKEEPERS, t in TIME} (t*schedule[r,h,t]) + cleanTime[r] - 1;

subject to One_Job_At_A_Time {h in HOUSEKEEPERS, t in TIME}:
	sum {r in ROOMS, pt in TIME: pt < t && pt >= max(0,t-cleanTime[r])} schedule[r,h,pt] <= 1;
	
subject to Max_Completion_Time {r in ROOMS}:
    completion[r] <= maxCompletion;
    
subject to Waiting_Time {r in ROOMS}:
	overlap[r] >= completion[r] - deadline[r] + 1;

subject to Tardiness {r in ROOMS}:
	overlap[r] >= 0;
	
subject to Max_Waiting_Time {r in ROOMS}:
	overlap[r] <= maxOverlap;