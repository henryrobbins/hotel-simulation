# This AMPL model is used to make an optimal assignment of guests to hotel rooms

set ROOMS; # set of hotel rooms 
set GUESTS ordered; # set of hotel guests 
#NOTE: if more than two people will share a room, they are considered a single guest

# Hotel room parameters
param type {ROOMS} integer > 0; # room type (higher value indicates nicer room)
# these parameters are only used when considering a housekeepign schedule
param release {ROOMS} integer >= 0; # time previous guest checks out; room is available for cleaning 
param process {ROOMS} integer > 0; # time the room takes to clean 
param completion {r in ROOMS} default release[r] + process[r]; # time the room becomes available for checkin

# Hotel guest parameters
param request {GUESTS} integer > 0; # the room type the hotel guest is requesting
param arrival {GUESTS} integer >= 0; # the time the hotel guest arrives
param weight {GUESTS,ROOMS} >= 0; # the satisfaction the hotel guest will have with each room

# Misc. parameters
param alpha >= 0 default 1; # parameter used in various weighted objective functions
param beta >= 0 default 1; # parameter used in various weighted objective functions
param gamma >= 0 default 1; # parameter used in various weighted objective functions
param tau >= 0, <= 1 default 0; # a threshold of guest satisfaction 
param minMeanMatchingWeight >= 0, <= 1 default 0; # the minimum mean satisfaction of a feasible matching
param prev {GUESTS, ROOMS} binary default 0; # 1 iff guest was assigned to room in previous assignemnt. 0 otherwise
param preservedEdges >= 0 default 0; # the minimum number of previous assignments that remain unchanged
param guest symbolic in GUESTS default first(GUESTS); # a specific hotel guest

# Decision variables
var assign {g in GUESTS, r in ROOMS} integer >= 0, <= 1; # 1 iff guest assigned to room. 0 otherwise
var I {g in GUESTS} binary; # 1 iff guest assigned to a room for which their satisfaction is < tau. 0 otherwise
var minWeight >= 0; # the minimum satisfaction of all guest 
var tardiness {g in GUESTS} integer >= 0; # the time every hotel guest will wait until their assigned room is ready
var maxTardiness integer >= 0; # the longest time a guest will wait until their assigned room is ready

# Objective functions
maximize Mean_Satisfaction: sum {g in GUESTS, r in ROOMS} assign[g,r]*weight[g,r]; # max the mean satisfaction across all guests
maximize Min_Satisfaction: minWeight; # max the satisfaction of the least satisfied guest
minimize Below_Tau: sum {g in GUESTS} I[g]; # min the number of guests with satisfactions < tau
minimize Upgrades: sum {g in GUESTS, r in ROOMS} assign[g,r]*(type[r] - request[g]); # min the number of guests upgraded to higher room types
minimize Mean_Wait_Time: sum {g in GUESTS} tardiness[g]; # min the mean time a guest waits for their room to be ready
minimize Max_Wait_Time: maxTardiness; # min the maximum time a guest waits for their room to be ready
maximize Guest_Satisfaction: sum {r in ROOMS} assign[guest, r]*weight[guest,r]; # max the satisfaction of the given guest
maximize Preserved_Edges: sum {g in GUESTS, r in ROOMS} assign[g,r]*prev[g,r]; # max the number of perserved assignments 
maximize Feasible: 0; # feasibility objective

# Weighted objective functions
# max Mean_Satisfaction and Min_Satisfaction
maximize Mean_And_Min_Satisfaction: 
	alpha*sum {g in GUESTS, r in ROOMS} assign[g,r]*weight[g,r] 
	+ beta*minWeight;
# max Mean_Satisfaction and Min_Satisfaction and min Upgrades
maximize Mean_Min_Sat_And_Upgrades: 
	alpha*(sum {g in GUESTS, r in ROOMS} assign[g,r]*weight[g,r]) 
	+ beta*(minWeight*card(GUESTS)) 
	- gamma*(sum {g in GUESTS, r in ROOMS} assign[g,r]*(type[r] - request[g]));
# max Mean_Satisfaction and min Below_Tau
maximize Mean_And_Below_Tau: 
	alpha*(sum {g in GUESTS, r in ROOMS} assign[g,r]*weight[g,r]) 
	- beta*(sum {g in GUESTS} I[g]);
# max Mean_Satisfaction and min Below_Tau and Upgrades
maximize Mean_Below_Tau_And_Upgrades: 
	alpha*(sum {g in GUESTS, r in ROOMS} assign[g,r]*weight[g,r]) 
	- beta*(sum {g in GUESTS} I[g])
	- gamma*(sum {g in GUESTS, r in ROOMS} assign[g,r]*(type[r] - request[g]));
# max Mean_Satisfaction and min Mean_Wait_Time
maximize Mean_Satisfaction_And_Wait_Time: 
	alpha*(sum {g in GUESTS, r in ROOMS} assign[g,r]*weight[g,r]) 
	- beta*(sum {g in GUESTS} tardiness[g]);
	
# Every guest is assigned to exactly one room
subject to Perfect_Matching {g in GUESTS}:
	sum {r in ROOMS} assign[g,r] = 1;
	
# Every room is assigned to at most one guest
subject to Room_Capacity {r in ROOMS}:
	sum {g in GUESTS} assign[g,r] <= 1;
	
# Every guest is assigned a room of their requested type or higher
subject to Requested_Type {g in GUESTS}: 
	sum {r in ROOMS} assign[g,r]*type[r] >= request[g]; 
	
# The number of preserved assignments is equal to or greater than the minimum allowed
subject to Preserve_Assignments:
	sum {g in GUESTS, r in ROOMS} assign[g,r]*prev[g,r] >= preservedEdges;
	
# The satisfaction of every guest is equal to or greater than the minimum satisfaction
subject to Minimum_Weight {g in GUESTS}:
    sum {r in ROOMS} assign[g,r]*weight[g,r] >= minWeight;
    
# The mean satisfaction is equal to or greater than the minimum allowed
subject to Minimum_Mean_Matching_Weight:
    (sum {g in GUESTS, r in ROOMS} assign[g,r]*weight[g,r])/card(GUESTS) >= minMeanMatchingWeight;
    
# I[g] = 1 exactly when the satisfaction of guest g is strictly less than tau
subject to Meeting_Tau {g in GUESTS}:
	sum {r in ROOMS} assign[g,r]*weight[g,r] + I[g] >= tau;
   
# Correctly defines tardiness as max(0,lateness)
subject to Lateness {g in GUESTS}:
	tardiness[g] >= (sum {r in ROOMS} assign[g,r]*completion[r]) - arrival[g] + 1;
subject to Tardiness {g in GUESTS}:
	tardiness[g] >= 0;
	
# The wait time of every guest is less than or equal to the maximum wait time
subject to Max_Tardiness {g in GUESTS}:
	tardiness[g] <= maxTardiness;