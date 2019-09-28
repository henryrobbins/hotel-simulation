set ROOMS;
set HOUSEKEEPERS;
set ORDER;

param roomType {ROOMS} > 0;
param checkOut {ROOMS} >= 0;
param cleanTime {ROOMS} > 0;

var assign {r in ROOMS, h in HOUSEKEEPERS, o in ORDER} integer >=0, <= 1;
var startTime {h in HOUSEKEEPERS, o in ORDER} integer >= 0;
var endTime {h in HOUSEKEEPERS, o in ORDER} integer >= 0;

minimize completionTime: sum {h in HOUSEKEEPERS, o in ORDER} endTime[h,o];

subject to Every_Room_Cleaned_Once {r in ROOMS}:
	sum {h in HOUSEKEEPERS, o in ORDER} assign[r,h,o]= 1;
	
subject to One_Room {h in HOUSEKEEPERS, o in ORDER}:
	sum {r in ROOMS} assign[r,h,o]<= 1;
	
subject to After_Checkout {h in HOUSEKEEPERS, o in ORDER}:
	startTime[h,o] >= sum {r in ROOMS} (checkOut[r]*assign[r,h,o])+1;
	
subject to Clean_Time {h in HOUSEKEEPERS, o in ORDER}:
	endTime[h,o]= startTime[h,o] + sum {r in ROOMS} (cleanTime[r]*assign[r,h,o])-1;
	
subject to One_At_A_Time {h in HOUSEKEEPERS, o in ORDER: o > 1}:
	startTime[h,o] >= endTime[h,o-1]+1;