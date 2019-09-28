set ROOMS;
set GUESTS ordered;

param roomType {ROOMS} > 0;
param checkout {ROOMS} >= 0;
param cleanTime {ROOMS} > 0;

param requestType {GUESTS} > 0;
param checkin {GUESTS} >= 0;

param preferences {ROOMS,GUESTS} >= 0;
param satisfaction {ROOMS,GUESTS} >= 0;

var assign {r in ROOMS, g in GUESTS} integer >= 0, <= 1;
var minSatisfaction >= 0;

maximize Min_Satisfaction: minSatisfaction;

subject to Accommodate_Everyone {g in GUESTS}:
	sum {r in ROOMS} assign[r,g] = 1;
	
subject to Room_Capacity {r in ROOMS}:
	sum {g in GUESTS} assign[r,g] <= 1;
	
subject to Requested_Type {g in GUESTS}: 
	sum {r in ROOMS} assign[r,g]*roomType[r] >= requestType[g]; 
	
subject to Minimum_Satisfaction {g in GUESTS}:
    sum {r in ROOMS} assign[r,g]*satisfaction[r,g] >= minSatisfaction;
    
