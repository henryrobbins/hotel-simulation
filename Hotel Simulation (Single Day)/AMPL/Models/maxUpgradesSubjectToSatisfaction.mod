set GUESTS;
set ROOMS;

param roomType {ROOMS} > 0;
param guestType {GUESTS} > 0;

param metPreferences {ROOMS,GUESTS} >= 0;
param satisfaction {ROOMS,GUESTS} >= 0;
param minimum >= 0;
param average >= 0;

var assign {r in ROOMS, g in GUESTS} integer >= 0, <= 1;

minimize upgrades: sum {r in ROOMS, g in GUESTS} assign[r,g]*roomType[r] - sum {r in ROOMS, g in GUESTS} assign[r,g]*guestType[g]; 

subject to One_Room_Per_Customer {g in GUESTS}:
	sum {r in ROOMS} assign[r,g] = 1;
	
subject to Room_Capacity {r in ROOMS}:
	sum {g in GUESTS} assign[r,g] <= 1;
	
subject to Type {g in GUESTS}: 
	sum {r in ROOMS} assign[r,g]*roomType[r] >= guestType[g]; 
	
subject to Minimum {g in GUESTS}:
    sum {r in ROOMS} assign[r,g]*satisfaction[r,g] >= minimum;
    
subject to Average:
	((sum {g in GUESTS, r in ROOMS} assign[r,g]*satisfaction[r,g])/card(GUESTS)) >= average;