package game;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;

import main.WindowManager;

/** Checks mouse position and highlights units as necessary. Sets units selection status **/
public class SelectionManager
{
	Main main;

	final int highlightThreshold = 50;
	public boolean drawSelection = false;
	public int[] selection = new int[4];
	int lastKeyPressed = 0;
	long lastTimePressed = 0;
	int doubleClickTimeout = 1000;
	boolean justReleased = false;

	ArrayList<Unit> selectionUnit1 = new ArrayList<Unit>();
	ArrayList<Unit> selectionUnit2 = new ArrayList<Unit>();
	ArrayList<Unit> selectionUnit3 = new ArrayList<Unit>();
	ArrayList<Unit> selectionUnit4 = new ArrayList<Unit>();
	ArrayList<Unit> selectionUnit5 = new ArrayList<Unit>();
	ArrayList<Unit> selectionUnit6 = new ArrayList<Unit>();
	ArrayList<Unit> selectionUnit7 = new ArrayList<Unit>();
	ArrayList<Unit> selectionUnit8 = new ArrayList<Unit>();
	ArrayList<Unit> selectionUnit9 = new ArrayList<Unit>();

	public SelectionManager(Main main)
	{
		this.main = main;
	}

	public void update()
	{
		keyboardUnitSelect();

		/**
		 * selectingState stores the current method of selection. 0 means no selection is being (no clicks happening). 1 means a normal left click has occured, and the highlighted unit should be selected. 2 means CTRL is being held down,
		 * and previously selected units should not be deselected. 3 means a selection box is being drawn. 4 is a selecting box which doesn't remove previous selections
		 **/
		int selectingState = 0;
		double[] cursorPos = WindowManager.getCursorPosition();

		if (WindowManager.getMouseButtonState(GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS)
		{
			// If this is the first press of the mouse (it has not been held down) set the selection box start coordinates to the current cursor location
			if (drawSelection == false)
			{
				selection[0] = (int) cursorPos[0];
				selection[1] = (int) cursorPos[1];
				drawSelection = true;
			}

			// Update the selection box end coordinates every frame that the mouse is held down
			selection[2] = (int) cursorPos[0];
			selection[3] = (int) cursorPos[1];

			boolean ctrlPressed = WindowManager.getKeyState(GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS;

			if (ctrlPressed)
				selectingState = 2;
			else
				selectingState = 1;

			// If a box is being drawn i.e. not just a single unit is being clicked on
			if (selection[0] != selection[2] || selection[1] != selection[3])
			{
				if (ctrlPressed)
					selectingState = 4;
				else
					selectingState = 3;
			}
		}
		else if (WindowManager.getMouseButtonState(GLFW_MOUSE_BUTTON_LEFT) == GLFW_RELEASE)
		{
			drawSelection = false;
		}

		double closestUnitDistance = Double.MAX_VALUE;
		Unit closestUnit = null;
		for (Unit unit : main.friendly)
		{
			// If we are just selecting individual units, deselect all others
			if (selectingState == 1)
				unit.deselect();
			unit.clearHighlighted();

			// If a selection box is being drawn
			if (selectingState == 3 || selectingState == 4)
			{
				// Select any units inside the selection box
				if (unit.x < Math.max(selection[0], selection[2]) && unit.x > Math.min(selection[0], selection[2]) && unit.y < Math.max(selection[1], selection[3]) && unit.y > Math.min(selection[1], selection[3]))
					unit.select();
				else if (selectingState == 3) // If CTRL is not pressed, deselect any units no longer in the selection box
					unit.deselect();
				continue;
			}

			double distance = Algorithms.distanceBetween(unit.x, unit.y, cursorPos[0], cursorPos[1]);
			if (distance < highlightThreshold)
			{
				if (distance < closestUnitDistance)
				{
					closestUnitDistance = distance;
					closestUnit = unit;
				}
			}
		}

		if (closestUnit != null)
		{
			if (selectingState > 0)
				closestUnit.select();
			else
				closestUnit.setHighlighted();
		}
	}

	private void keyboardUnitSelect()
	{
		if (checkUnitSelect(1, GLFW_KEY_1, selectionUnit1))
			return;
		if (checkUnitSelect(2, GLFW_KEY_2, selectionUnit2))
			return;
		if (checkUnitSelect(3, GLFW_KEY_3, selectionUnit3))
			return;
		if (checkUnitSelect(4, GLFW_KEY_4, selectionUnit4))
			return;
		if (checkUnitSelect(5, GLFW_KEY_5, selectionUnit5))
			return;
		if (checkUnitSelect(6, GLFW_KEY_6, selectionUnit6))
			return;
		if (checkUnitSelect(7, GLFW_KEY_7, selectionUnit7))
			return;
		if (checkUnitSelect(8, GLFW_KEY_8, selectionUnit8))
			return;
		if (checkUnitSelect(9, GLFW_KEY_9, selectionUnit9))
			return;
	}

	private boolean checkUnitSelect(int i, int keyCode, ArrayList<Unit> unitSelection)
	{
		if (WindowManager.getKeyState(keyCode) == GLFW_PRESS)
		{
			if (WindowManager.getKeyState(GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS)
			{
				unitSelection.clear();
				for (Unit unit : main.friendly)
				{
					if (unit.getSelected())
						unitSelection.add(unit);
				}
			}
			else
			{
				for (Unit unit : main.friendly)
					unit.deselect();

				boolean anyUnitsSelected = false;

				for (Unit unit : unitSelection)
				{
					unit.select();
					anyUnitsSelected = true;
				}

				if (doubleTapCheck(keyCode, true) && anyUnitsSelected)
					CameraManager.moveCameraTo(unitSelection.get(0).x - WindowManager.width / 2, unitSelection.get(0).y - WindowManager.height / 2);
			}
			return true;
		}
		doubleTapCheck(keyCode, false);
		return false;
	}

	private boolean doubleTapCheck(int keyCode, boolean pressed)
	{
		if (pressed == false)
		{
			if (lastKeyPressed == keyCode)
			{
				// Key has just been released
				if (justReleased)
				{
					lastTimePressed = System.currentTimeMillis();
					justReleased = false;
				}
			}
			return false;
		}

		if (lastKeyPressed != keyCode)
		{
			lastKeyPressed = keyCode;
			lastTimePressed = 0;
			justReleased = true;
			return false;
		}

		if (System.currentTimeMillis() - lastTimePressed > doubleClickTimeout)
		{
			return false;
		}

		// justReleased = false;
		// lastKeyPressed = 0;
		lastTimePressed = 0;
		return true;
	}
}
