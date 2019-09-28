set ROOMS;
set GUESTS;
set HOUSEKEEPERS;
set TIME ordered;

param roomType {ROOMS} integer > 0;
param checkout {ROOMS} integer >= 0;
param cleanTime {ROOMS} integer > 0;

param requestType {GUESTS} integer > 0;
param checkin {GUESTS} integer >= 0;

param preferences {ROOMS,GUESTS} integer >= 0;
param satisfaction {ROOMS,GUESTS} >= 0;
param meanSatisfaction default 0;

var schedule {r in ROOMS, h in HOUSEKEEPERS, t in TIME} integer >=0, <= 1;
var assign {r in ROOMS, g in GUESTS} integer >= 0, <= 1;
var completion {r in ROOMS} integer >= 0;
var maxCompletion integer >= 0;
var overlap {g in GUESTS} integer >= 0;
var maxOverlap integer >= 0;

minimize Mean_Wait_Time: sum {g in GUESTS} overlap[g];
minimize Max_Wait_Time: maxOverlap;
maximize Mean_Satisfaction: sum {g in GUESTS, r in ROOMS} assign[r,g]*satisfaction[r,g];
maximize Mean_Satisfaction_And_Wait_Time: sum {g in GUESTS, r in ROOMS} assign[r,g]*satisfaction[r,g] - (sum {g in GUESTS} overlap[g]);

# HOUSEKEEPING CONSTRAINTS

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
	
# ROOM ASSIGNMENT CONSTRAINTS

subject to Accommodate_Everyone {g in GUESTS}:
	sum {r in ROOMS} assign[r,g] = 1;
	
subject to Room_Capacity {r in ROOMS}:
	sum {g in GUESTS} assign[r,g] <= 1;
	
subject to Requested_Type {g in GUESTS}: 
	sum {r in ROOMS} assign[r,g]*roomType[r] >= requestType[g]; 
    
subject to Average_Satisfaction:
    (sum {g in GUESTS, r in ROOMS} assign[r,g]*satisfaction[r,g])/card(GUESTS) >= meanSatisfaction;
	
# OVERLAP
	
subject to Waiting_Time {g in GUESTS}:
	overlap[g] >= (sum {r in ROOMS} assign[r,g]*completion[r]) - checkin[g] + 1;

subject to Tardiness {g in GUESTS}:
	overlap[g] >= 0;
	
subject to Max_Waiting_Time {g in GUESTS}:
	overlap[g] <= maxOverlap;