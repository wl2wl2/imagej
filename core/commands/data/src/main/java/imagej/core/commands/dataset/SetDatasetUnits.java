package imagej.core.commands.dataset;

import imagej.command.Command;
import imagej.data.Dataset;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

@Plugin(menuPath = "Image>Units>Set Data Units")
public class SetDatasetUnits implements Command {

	@Parameter
	private Dataset dataset;

	@Parameter(label = "X Axis Unit")
	private String xUnit;

	@Parameter(label = "Y Axis Unit")
	private String yUnit;

	@Override
	public void run() {
		dataset.getImgPlus().axis(0).setUnit(xUnit);
		dataset.getImgPlus().axis(1).setUnit(yUnit);
	}

}
