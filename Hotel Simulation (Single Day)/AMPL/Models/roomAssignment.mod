set ROOMS;
set GUESTS ordered;

param roomType {ROOMS} integer > 0;
param checkout {ROOMS} integer >= 0;
param cleanTime {ROOMS} integer > 0;
param completion {r in ROOMS} default checkout[r];

param requestType {GUESTS} integer > 0;
param checkin {GUESTS}integer >= 0;

param preferences {ROOMS,GUESTS} integer >= 0;
param satisfaction {ROOMS,GUESTS} >= 0;
param meanSatisfaction default 0;
param guest symbolic in GUESTS default first(GUESTS); 

var assign {r in ROOMS, g in GUESTS} integer >= 0, <= 1;
var minPreferences integer >= 0;
var minSatisfaction >= 0;
var overlap {g in GUESTS} integer >= 0;
var maxOverlap integer >= 0;

maximize Mean_Preferences: sum {g in GUESTS, r in ROOMS} assign[r,g]*preferences[r,g];
maximize Min_Preferences: minPreferences;
maximize Mean_Satisfaction: sum {g in GUESTS, r in ROOMS} assign[r,g]*satisfaction[r,g];
maximize Min_Satisfaction: minSatisfaction;
maximize Satisfaction: sum {g in GUESTS, r in ROOMS} assign[r,g]*satisfaction[r,g] + minSatisfaction*card(GUESTS);
minimize Upgrades: sum {r in ROOMS, g in GUESTS} assign[r,g]*(roomType[r] - requestType[g]); 
minimize Mean_Wait_Time: sum {g in GUESTS} overlap[g];
minimize Max_Wait_Time: maxOverlap;
maximize Mean_Satisfaction_And_Wait_Time: sum {g in GUESTS, r in ROOMS} assign[r,g]*satisfaction[r,g] - (sum {g in GUESTS} overlap[g]);
maximize Guest_Satisfaction: sum {r in ROOMS} assign[r,guest]*satisfaction[r,guest];
maximize Feasible: 0;

subject to Accommodate_Everyone {g in GUESTS}:
	sum {r in ROOMS} assign[r,g] = 1;
	
subject to Room_Capacity {r in ROOMS}:
	sum {g in GUESTS} assign[r,g] <= 1;
	
subject to Requested_Type {g in GUESTS}: 
	sum {r in ROOMS} assign[r,g]*roomType[r] >= requestType[g]; 
	
subject to Minimum_Preferences {g in GUESTS}:
	sum {r in ROOMS} assign[r,g]*preferences[r,g] >= minPreferences;
	
subject to Minimum_Satisfaction {g in GUESTS}:
    sum {r in ROOMS} assign[r,g]*satisfaction[r,g] >= minSatisfaction;
    
subject to Average_Satisfaction:
    (sum {g in GUESTS, r in ROOMS} assign[r,g]*satisfaction[r,g])/card(GUESTS) >= meanSatisfaction;
   
subject to Waiting_Time {g in GUESTS}:
	overlap[g] >= (sum {r in ROOMS} assign[r,g]*completion[r]) - checkin[g] + 1;

subject to Tardiness {g in GUESTS}:
	overlap[g] >= 0;
	
subject to Max_Waiting_Time {g in GUESTS}:
	overlap[g] <= maxOverlap;