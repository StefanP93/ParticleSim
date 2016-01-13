#include <stdlib.h> 
#include <stdio.h> 
#include <string.h> 
#include <math.h> 
#include <sys/time.h> 
#include <unistd.h>  
#include <MaxSLiCInterface.h>  
#include "DataDef.h"

#define ACCURACY 0.1

//timer functions
void timeStart(double *timeV){
    struct timeval timeV; 
    gettimeofday(&timeV, NULL); 
    *timeV -= timeV.tv_sec + timeV.tv_usec * 1e-6; 

}
void timeStop(double *timeV){
    struct timeval timeV; 
    gettimeofday(&timeV, NULL); 
    *timeV += timeV.tv_sec + timeV.tv_usec * 1e-6; 
}

void int check(particle *ref,particle *dfe){
    int i;

    for(i = 0; i < particlesNmb ; i++)
    {
        if(abs(ref[i].pos.x-dfe[i].pos.x)> ACCURACY ) return 0;
        if(abs(ref[i].pos.y-dfe[i].pos.y)> ACCURACY ) return 0;
        if(abs(ref[i].pos.z-dfe[i].pos.z)> ACCURACY ) return 0;
    }

    return 1;

}

int main(int argc, char** argv){

    int i, *randArray;
    particle *particles,*p,*a,*pDfe;
    double *timeVal;
    float *inX,*inY,*inZ,*inM,*outX,*outY,*outZ,*outM;
    int result = -1;
    particles = (particle *) malloc(particlesNmb * sizeof(particle));
    p = (particle *) malloc(particlesNmb * sizeof(particle)); //Cpu arrays
    pDfe = (particle *) malloc(particlesNmb * sizeof(particle)); //Cpu arrays
    a = (particle *) malloc(particlesNmb * sizeof(particle)); //Dfe array

    inX = (float *) malloc(particlesNmb * sizeof(float));
    inY = (float *) malloc(particlesNmb * sizeof(float));
    inZ = (float *) malloc(particlesNmb * sizeof(float));
    inM = (float *) malloc(particlesNmb * sizeof(float));

    outX = (float *) malloc(particlesNmb * sizeof(float));
    outY = (float *) malloc(particlesNmb * sizeof(float));
    outZ = (float *) malloc(particlesNmb * sizeof(float));
    outM = (float *) malloc(particlesNmb * sizeof(float));

    //init particles with random values

    srand(100); 
    for ( i = 0; i < particlesNmb ; i++) 
    { 
        pDfe[i].mass = inM[i] = p[i].mass = particles[i].mass = (float)rand()/100000; 
        pDfe[i].pos.x = inX[i] = p[i].pos.x = particles[i].pos.x = (float)rand()/100000; 
        pDfe[i].pos.y = inY[i] = p[i].pos.y = particles[i].pos.y = (float)rand()/100000; 
        pDfe[i].pos.z = inZ[i] = p[i].pos.z = particles[i].pos.z = (float)rand()/100000; 
        pDfe[i].vel.x = p[i].vel.x = particles[i].vel.x = (float)rand()/100000; 
        pDfe[i].vel.y = p[i].vel.y = particles[i].vel.y = (float)rand()/100000; 
        pDfe[i].vel.z = p[i].vel.z = particles[i].vel.z = (float)rand()/100000; 

        a[i] = 0;
    } 

    


    timeStart(timeV);
    //CPU code to get reference time
    for (int t = 0; t < timeSteps; t++) {  
 
        for (int q = 0; q < particlesNmb; q++) { 
            for (int j = 0; j < particlesNmb; j++) { 
                
                     float rx = p[j].pos.x - p[q].pos.x; 
                     float ry = p[j].pos.y - p[q].pos.y; 
                     float rz = p[j].pos.z - p[q].pos.z; 
                     float dd = rx*rx + ry*ry + rz*rz; 
                     if(dd!=0){
                        float d = 1/ (dd*sqrtf(dd)); 
                        float s = p[j].mass * d; 
                        a[q].x += rx * s; 
                        a[q].y += ry * s; 
                        a[q].z += rz * s;
                     } 
                     else{
                       
                       
                            if(p[q].mass<p[j].mass){
                                p[j].mass += p[q].mass*0.01;
                                p[q].mass = 0;
                            }
                            else{
                                p[q].mass += p[j].mass*0.01;
                                p[j].mass = 0;
                            }


                        }
                     }
                 
            
        } 
 
 
        for (int i = 0; i < particlesNmb; i++) { 
             p[i].p.x += p[i].v.x; 
             p[i].p.y += p[i].v.y; 
             p[i].p.z += p[i].v.z; 
             p[i].v.x += a[i].x; 
             p[i].v.y += a[i].y; 
             p[i].v.z += a[i].z; 
        } 


     } 
     timeStop(timeV);

     printf("CPU time: %lf",timeV);

     timeStart(timeV);
    //DFE code
    for (int t = 0; t < timeSteps; t++) {  
 
        printf("Running DFE.\n");
        DFE(particlesNmb, inX, inY, inZ, inM,inX, inY, inZ, inM, outX, outY ,outZ, outM);
 
        for (int i = 0; i < particlesNmb; i++) { 
             pDfe[i].p.x += pDfe[i].v.x; 
             pDfe[i].p.y += pDfe[i].v.y; 
             pDfe[i].p.z += pDfe[i].v.z; 
             pDfe[i].v.x += outX[i]; 
             pDfe[i].v.y += outY[i]; 
             pDfe[i].v.z += outZ[i]; 
             pDfe[i].mass = outM[i];

             inX[i] = pDfe[i].p.x;
             inY[i] = pDfe[i].p.y;
             inZ[i] = pDfe[i].p.z;
             inM[i] = pDfe[i].mass;
        } 


     } 
     timeStop(timeV);

     printf("DFE time: %lf",timeV);

     result = check (p,pDfe);

     if(result == 0) printf("\n TEST FAILED");
     if(result == 1) printf("\n TEST PASSED");

    return 1;

} 
 
