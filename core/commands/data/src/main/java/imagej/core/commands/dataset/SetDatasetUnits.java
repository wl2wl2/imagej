package imagej.core.commands.dataset;

import imagej.command.Command;
import imagej.data.Dataset;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

@Plugin(menuPath = "Plugins>Sandbox>Set Data Units")
public class SetDatasetUnits implements Command {

	@Parameter
	private Dataset dataset;

	@Parameter(label = "X Axis Unit")
	private String xUnit;

	@Parameter(label = "Y Axis Unit")
	private String yUnit;

	@Override
	public void run() {
		dataset.getImgPlus().setCalibrationUnit(xUnit, 0);
		dataset.getImgPlus().setCalibrationUnit(yUnit, 1);
	}

}
