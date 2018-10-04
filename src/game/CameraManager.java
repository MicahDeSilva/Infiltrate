package game;

import static org.lwjgl.glfw.GLFW.*;

import main.WindowManager;

public class CameraManager
{
	static double cameraX = 0, cameraY = 0;
	static boolean middleClicked = false;
	static double clickX;
	static double clickY;
	static double maxCameraX = Main.mapWidth - WindowManager.width;
	static double maxCameraY = Main.mapHeight - WindowManager.height;

	public static double[] updateCameraPos()
	{
		if (WindowManager.getMouseButtonState(GLFW_MOUSE_BUTTON_MIDDLE) == GLFW_PRESS)
		{
			double[] cursorPos = WindowManager.getRawCursorPosition();

			if (middleClicked == false)
			{
				clickX = cameraX + cursorPos[0];
				clickY = cameraY + cursorPos[1];
				middleClicked = true;
			}

			if (middleClicked)
			{
				cameraX = clickX - cursorPos[0];
				cameraY = clickY - cursorPos[1];
			}

			cameraX = Math.max(cameraX, -WindowManager.width / 2);
			cameraY = Math.max(cameraY, -maxCameraY - WindowManager.height);
			cameraX = Math.min(cameraX, maxCameraX + WindowManager.width / 2);
			cameraY = Math.min(cameraY, maxCameraY);
		}
		else if (WindowManager.getMouseButtonState(GLFW_MOUSE_BUTTON_MIDDLE) == GLFW_RELEASE)
		{
			middleClicked = false;
		}

		// cameraX -= 4;

		double[] cameraPos =
		{
				cameraX, cameraY
		};
		return cameraPos;

	}

	public static void moveCameraTo(double newCameraX, double newCameraY)
	{
		cameraX = newCameraX;
		cameraY = newCameraY;
	}

}
