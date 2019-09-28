set ROOMS;
set GUESTS;
set HOUSEKEEPERS;
set TIME;
set TYPE = 1 .. 8 by 1;

param roomType {ROOMS} > 0;
param checkout {ROOMS} >= 0;
param cleanTime {ROOMS} > 0;

param requestType {GUESTS} integer > 0;
param checkin {GUESTS}integer >= 0;

param preferences {ROOMS,GUESTS} integer >= 0;
param satisfaction {ROOMS,GUESTS} >= 0;
param numberNeeded {TIME, TYPE} >= 0;

var schedule {r in ROOMS, h in HOUSEKEEPERS, t in TIME} integer >=0, <= 1;
var completion {r in ROOMS} integer >= 0;

minimize completionTime: sum {r in ROOMS} completion[r];

subject to After_Checkout {r in ROOMS}:
	checkout[r]+1 <= sum {h in HOUSEKEEPERS, t in TIME} t*schedule[r,h,t];

subject to Every_Room_Cleaned_Once {r in ROOMS}:
	sum {h in HOUSEKEEPERS, t in TIME} schedule[r,h,t]= 1;
	
subject to Clean_Time {r in ROOMS}:
	completion[r]= sum {h in HOUSEKEEPERS, t in TIME} (t*schedule[r,h,t]) + cleanTime[r] - 1;

subject to One_At_A_Time {h in HOUSEKEEPERS, t in TIME}:
	sum {r in ROOMS, pt in TIME: pt < t && pt >= max(0,t-cleanTime[r])} schedule[r,h,pt] <= 1;
	
subject to Number_Of_Type_Needed {t in TIME, type in TYPE}:
	sum {r in ROOMS: roomType[r]= type} (sum { h in HOUSEKEEPERS, pt in TIME: pt >= 0 && pt <= max(0,t-cleanTime[r])} schedule[r,h,pt]) >= numberNeeded[t,type];