set ROOMS;
set GUESTS ordered;

param type {ROOMS} integer > 0;
param release {ROOMS} integer >= 0;
param process {ROOMS} integer > 0;
param completion {r in ROOMS} default release[r] + process[r];

param request {GUESTS} integer > 0;
param arrival {GUESTS} integer >= 0;

param weight {GUESTS,ROOMS} >= 0;
param prev {GUESTS, ROOMS} binary default 0;
param preservedEdges >= 0 default 0;
param minMeanMatchingWeight default 0;
param guest symbolic in GUESTS default first(GUESTS); 

var assign {g in GUESTS, r in ROOMS} integer >= 0, <= 1;
var minWeight >= 0;
var tardiness {g in GUESTS} integer >= 0;
var maxTardiness integer >= 0;

maximize Mean_Satisfaction: sum {g in GUESTS, r in ROOMS} assign[g,r]*weight[g,r];
maximize Min_Satisfaction: minWeight;
maximize Satisfaction: sum {g in GUESTS, r in ROOMS} assign[g,r]*weight[g,r] + minWeight*card(GUESTS);
minimize Upgrades: sum {g in GUESTS, r in ROOMS} assign[g,r]*(type[r] - request[g]); 
minimize Mean_Wait_Time: sum {g in GUESTS} tardiness[g];
minimize Max_Wait_Time: maxTardiness;
maximize Mean_Satisfaction_And_Wait_Time: sum {g in GUESTS, r in ROOMS} assign[g,r]*weight[g,r] - (sum {g in GUESTS} tardiness[g]);
maximize Guest_Satisfaction: sum {r in ROOMS} assign[guest, r]*weight[guest,r];
maximize Preserved_Edges: sum {g in GUESTS, r in ROOMS} assign[g,r]*prev[g,r];
maximize Feasible: 0;

subject to Perfect_Matching {g in GUESTS}:
	sum {r in ROOMS} assign[g,r] = 1;
	
subject to Room_Capacity {r in ROOMS}:
	sum {g in GUESTS} assign[g,r] <= 1;
	
subject to Requested_Type {g in GUESTS}: 
	sum {r in ROOMS} assign[g,r]*type[r] >= request[g]; 
	
subject to Preserve_Assignments:
	sum {g in GUESTS, r in ROOMS} assign[g,r]*prev[g,r] >= preservedEdges;
	
subject to Minimum_Weight {g in GUESTS}:
    sum {r in ROOMS} assign[g,r]*weight[g,r] >= minWeight;
    
subject to Minimum_Mean_Matching_Weight:
    (sum {g in GUESTS, r in ROOMS} assign[g,r]*weight[g,r])/card(GUESTS) >= minMeanMatchingWeight;
   
subject to Lateness {g in GUESTS}:
	tardiness[g] >= (sum {r in ROOMS} assign[g,r]*completion[r]) - arrival[g] + 1;

subject to Tardiness {g in GUESTS}:
	tardiness[g] >= 0;
	
subject to Max_Tardiness {g in GUESTS}:
	tardiness[g] <= maxTardiness;