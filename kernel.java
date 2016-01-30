import com.maxeler.maxcompiler.v2.kernelcompiler.Kernel;
import com.maxeler.maxcompiler.v2.kernelcompiler.KernelParameters;
import com.maxeler.maxcompiler.v2.kernelcompiler.stdlib.core.Count.Counter;
import com.maxeler.maxcompiler.v2.kernelcompiler.stdlib.core.Count.Params;
import com.maxeler.maxcompiler.v2.kernelcompiler.stdlib.memory.Memory;
import com.maxeler.maxcompiler.v2.kernelcompiler.types.base.DFEVar;
import com.maxeler.maxcompiler.v2.utils.MathUtils;
import com.maxeler.maxcompiler.v2.kernelcompiler.stdlib.KernelMath;

class ParticleSimKernel extends Kernel {
	ParticleSimKernel(KernelParameters parameters) {
		super(parameters);

		int DATA_SIZE = 1024;
	
		int addrBits = MathUtils.bitsToAddress(DATA_SIZE);
		Params addressCounterParams = control.count.makeParams(addrBits+1);
		Counter addressCounter = control.count.makeCounter(addressCounterParams);

		
		DFEVar xAddress = addressCounter.getCount();
		xAddress = xAddress.cast(dfeUInt(addrBits));

		DFEVar yAddress = addressCounter.getCount() + addrBits ;
		yAddress = yAddress.cast(dfeUInt(addrBits));

		DFEVar zAddress = addressCounter.getCount() + 2*addrBits;
		zAddress = zAddress.cast(dfeUInt(addrBits));

		DFEVar mAddress = addressCounter.getCount() + 3*addrBits;
		mAddress = mAddress.cast(dfeUInt(addrBits));
		
		DFEVar readingInput = addressCounter.getCount() < DATA_SIZE;

		
		DFEVar x = io.input("pX", dfeFloat(8,24), readingInput);
		DFEVar y = io.input("pY", dfeFloat(8,24), readingInput);
		DFEVar z = io.input("pZ", dfeFloat(8,24), readingInput);
		DFEVar m = io.input("pM", dfeFloat(8,24), readingInput);
	

		Memory<DFEVar> Ram = mem.alloc(dfeFloat(8,24), 4*DATA_SIZE);
		
		
		Ram.write(xAddress, x, readingInput);
		Ram.write(yAddress, y, readingInput);
		Ram.write(zAddress, z, readingInput);
		Ram.write(mAddress, m, readingInput);

		DFEVar px = io.input("piX", dfeFloat(8,24), ~readingInput);
		DFEVar py = io.input("piY", dfeFloat(8,24), ~readingInput);
		DFEVar pz = io.input("piZ", dfeFloat(8,24), ~readingInput);
		DFEVar pm = io.input("piM", dfeFloat(8,24), ~readingInput);

		DFEVar ax = io.scalarInput("zero", dfeFloat(8, 24)); 
		DFEVar ay = io.scalarInput("zero", dfeFloat(8, 24));
		DFEVar az = io.scalarInput("zero", dfeFloat(8, 24)); 
		DFEVar am = io.scalarInput("zero", dfeFloat(8, 24));
		DFEVar zeroM = io.scalarInput("zero", dfeFloat(8, 24)); 

		for(int i=0;i<DATA_SIZE;i++){

				DFEVar qX = Ram.read(xAddress + i);
				DFEVar qY = Ram.read(yAddress + i);
				DFEVar qZ = Ram.read(zAddress + i);
				DFEVar qM = Ram.read(mAddress + i);

				DFEVar rx = qX - px; 
                DFEVar ry = qY - py;  
                DFEVar rz = qZ - pz;  
                DFEVar dd = rx*rx + ry*ry + rz*rz; 

                ax += dd === 0 ? 0 : (rx * qM *  1/ (dd*KernelMath.sqrt(dd))); 
                ay += dd === 0 ? 0 : (ry * qM *  1/ (dd*KernelMath.sqrt(dd))); 
                az += dd === 0 ? 0 : (rz * qM *  1/ (dd*KernelMath.sqrt(dd)));
                am += dd === 0 ? qM : 0;

                DFEVar collision = dd === 0;

               Ram.write(mAddress+i, zeroM, collision);
               Ram.write(mAddress+i, qM, collision);

		}

		io.output("xData", ax, dfeFloat(8,24),~readingInput);
		io.output("yData", ay, dfeFloat(8,24),~readingInput);
		io.output("zData", az, dfeFloat(8,24),~readingInput);
		io.output("mData", am, dfeFloat(8,24),~readingInput);
	}
}