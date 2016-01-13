#ifndef DATADEF_H_
#define DATADEF_H_


#define particlesNmb 1024
#define timeSteps 20

typedef struct { 
   float x; 
   float y; 
   float z; 
} coord; 

 

typedef struct { 
    coord pos; 
    coord vel;
    double mass; 
} particle; 



#endif 