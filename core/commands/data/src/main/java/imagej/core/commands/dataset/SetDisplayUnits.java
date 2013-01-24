package imagej.core.commands.dataset;

import imagej.command.Command;
import imagej.data.display.ImageDisplay;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import net.imglib2.meta.Axes;

@Plugin(menuPath = "Image>Units>Set Display Units")
public class SetDisplayUnits implements Command {

	@Parameter
	private ImageDisplay display;

	@Parameter(label = "X Axis Display Unit")
	private String xUnit;

	@Parameter(label = "Y Axis Display Unit")
	private String yUnit;

	@Override
	public void run() {
		display.setUnit(Axes.X, xUnit);
		display.setUnit(Axes.Y, yUnit);
	}

}
