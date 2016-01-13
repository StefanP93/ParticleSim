import com.maxeler.maxcompiler.v2.build.EngineParameters;
import com.maxeler.maxcompiler.v2.kernelcompiler.Kernel;
import com.maxeler.maxcompiler.v2.managers.engine_interfaces.CPUTypes;
import com.maxeler.maxcompiler.v2.managers.engine_interfaces.EngineInterface;
import com.maxeler.maxcompiler.v2.managers.engine_interfaces.InterfaceParam;
import com.maxeler.maxcompiler.v2.managers.standard.Manager;
import com.maxeler.maxcompiler.v2.managers.standard.Manager.IOType;

class ParticleSimManager {

	public static void main(String[] args) {
		Manager m = new Manager(new EngineParameters(args));
		Kernel k = new ParticleSimKernel(m.makeKernelParameters());
		m.setKernel(k);
		m.setIO(IOType.ALL_CPU);
		m.createSLiCinterface(modeDefault());
		m.build();
	}

	private static EngineInterface modeDefault() {
		EngineInterface ei = new EngineInterface();

		InterfaceParam n = ei.addParam("N", CPUTypes.INT);
		ei.setTicks("ParticleSimKernel", n * 2);

		CPUTypes addressType = CPUTypes.FLOAT;
		InterfaceParam sizeBytes = n * addressType.sizeInBytes();
		ei.setStream("pX", addressType, sizeBytes);
		ei.setStream("pY", addressType, sizeBytes);
		ei.setStream("pZ", addressType, sizeBytes);
		ei.setStream("pM", addressType, sizeBytes);

		ei.setStream("piX", addressType, sizeBytes);
		ei.setStream("piY", addressType, sizeBytes);
		ei.setStream("piZ", addressType, sizeBytes);
		ei.setStream("piM", addressType, sizeBytes);

		ei.setStream("xData", addressType, sizeBytes);
		ei.setStream("yData", addressType, sizeBytes);
		ei.setStream("zData", addressType, sizeBytes);
		ei.setStream("mData", addressType, sizeBytes);


		return ei;
	}

	
}
