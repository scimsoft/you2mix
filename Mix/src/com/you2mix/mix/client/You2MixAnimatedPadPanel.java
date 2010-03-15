package com.you2mix.mix.client;

import java.util.ArrayList;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.you2mix.mix.client.SurfaceView.You2MixVideoDataView;
import com.you2mix.mix.client.model.You2MixVideo;

public class You2MixAnimatedPadPanel extends FlowPanel {

	public interface VideoMixPositionObserver {
		void onUpdateMixPositions(ArrayList<You2MixVideo> videoDataList);
		void onInitialMixPositionsLoaded(ArrayList<You2MixVideo> videoDataList);
	}

	private int padWidth = 900;

	private int padHeight = 700;

	private int padCellWidth = 180;

	private int padCellHeight = 290;

	private int verticalOffset = 60;

	private int horizontalOffset = 10;

	private final static int HORIZONTAL_OUTOF_BOUND_POSITION = 99;
	private final static int VERTICAL_OUTOF_BOUND_POSITION = 99;

	private CellPosition[][] cellPositions;

	private Widget[][] cellWidgets;

	private int numHorizontalCells;

	private int numVerticalCells;

	protected VideoMixPositionObserver observer;

	public You2MixAnimatedPadPanel() {
		calculateCellPositions();
		this.setSize(Integer.toString(padWidth), Integer.toString(padHeight));
	}

	public void calculateCellPositions() {
		numHorizontalCells = (int) Math.ceil(padWidth / padCellWidth);
		numVerticalCells = (int) Math.ceil(padHeight / padCellHeight);
		cellPositions = new CellPosition[numHorizontalCells + 1][numVerticalCells + 1];
		cellWidgets = new Widget[numHorizontalCells][numVerticalCells];

		int verticalPosition = verticalOffset;
		for (int verticalCellNumber = 0; verticalCellNumber < numVerticalCells; verticalCellNumber++) {
			int horizontalPosition = horizontalOffset;
			for (int horizontalcellnumber = 0; horizontalcellnumber < numHorizontalCells; horizontalcellnumber++) {
				cellPositions[horizontalcellnumber][verticalCellNumber] = new CellPosition(horizontalPosition, verticalPosition);
				horizontalPosition += padCellWidth;
			}
			verticalPosition += padCellHeight;
		}

	}

	public void addFirstFreeCellPosition(Widget widget) {
		CellPosition firtsFreeCell = getfirstFreecellCellPosition(widget);
		positionWidget(widget, firtsFreeCell);

	}

	public void addSuper(Widget widget) {
		super.add(widget);
	}

	public void add(Widget widget) {
		super.add(widget);
		addFirstFreeCellPosition(widget);
	}

	public boolean remove(Widget widget) {
		removeCellwidget(widget);
		return super.remove(widget);
	}

	public boolean removeSuper(Widget widget) {
		return super.remove(widget);
	}

	private boolean removeCellwidget(Widget widget) {
		Widget[][] newCellWidgets = new Widget[numHorizontalCells][numVerticalCells];
		for (int verticalCellNumber = 0; verticalCellNumber < numVerticalCells; verticalCellNumber++) {
			for (int horizontalcellnumber = 0; horizontalcellnumber < numHorizontalCells; horizontalcellnumber++) {
				if (cellWidgets[horizontalcellnumber][verticalCellNumber] == widget) {
					for (int i = 0; i < numHorizontalCells; i++) {
						for (int ii = 0; ii < numVerticalCells; ii++) {
							if (!(i == horizontalcellnumber && ii == verticalCellNumber)) {
								newCellWidgets[i][ii] = cellWidgets[i][ii];
							}
						}
					}
					cellWidgets = newCellWidgets;
					return true;
				}
			}

		}
		return false;
	}

	public void setPadPixelPosition(Widget widget, int xPos, int yPos) {
		CellMatrixNumber newCell;
		newCell = calculateNearestCellPosition(xPos, yPos);
		removeCellwidget(widget);
		if (newCell.getY() == VERTICAL_OUTOF_BOUND_POSITION || newCell.getX() == HORIZONTAL_OUTOF_BOUND_POSITION) {
			newCell = getfirstFreecellCell(widget);
		}
		insertWidgetinCellPosistion(widget, newCell);
		if (observer != null) {
			this.observer.onUpdateMixPositions(getVideoList());
		}
	}

	private CellMatrixNumber getfirstFreecellCell(Widget widget) {
		for (int verticalCellNumber = 0; verticalCellNumber < numVerticalCells; verticalCellNumber++) {
			for (int horizontalcellnumber = 0; horizontalcellnumber < numHorizontalCells; horizontalcellnumber++) {
				if (cellWidgets[horizontalcellnumber][verticalCellNumber] == null) {
					cellWidgets[horizontalcellnumber][verticalCellNumber] = widget;
					return new CellMatrixNumber(horizontalcellnumber, verticalCellNumber);
				}

			}

		}
		return null;
	}

	private CellPosition getfirstFreecellCellPosition(Widget widget) {
		for (int verticalCellNumber = 0; verticalCellNumber < numVerticalCells; verticalCellNumber++) {
			for (int horizontalcellnumber = 0; horizontalcellnumber < numHorizontalCells; horizontalcellnumber++) {
				if (cellWidgets[horizontalcellnumber][verticalCellNumber] == null) {
					cellWidgets[horizontalcellnumber][verticalCellNumber] = widget;
					return cellPositions[horizontalcellnumber][verticalCellNumber];
				}

			}

		}
		return null;
	}

	private CellMatrixNumber calculateNearestCellPosition(int x, int y) {
		int newHorizontalCellNumber = HORIZONTAL_OUTOF_BOUND_POSITION;
		int newVerticalCellNumber = VERTICAL_OUTOF_BOUND_POSITION;
		for (int horizontalcellnumber = 0; horizontalcellnumber < numHorizontalCells - 1; horizontalcellnumber++) {
			CellPosition currentCellPosition = cellPositions[horizontalcellnumber][0];
			CellPosition nextCellPosition = cellPositions[horizontalcellnumber + 1][0];
			if (x < horizontalOffset) {
				newHorizontalCellNumber = 0;
			}
			if (currentCellPosition.getxPos() == x) {
				newHorizontalCellNumber = horizontalcellnumber;
				break;
			} else if (currentCellPosition.getxPos() < x && nextCellPosition.getxPos() >= x) {
				newHorizontalCellNumber = horizontalcellnumber + 1;
				break;
			}
		}
		for (int verticalCellNumber = 0; verticalCellNumber < numVerticalCells - 1; verticalCellNumber++) {
			CellPosition currentCellPosition = cellPositions[0][verticalCellNumber];
			CellPosition nextCellPosition = cellPositions[0][verticalCellNumber + 1];
			if (y < verticalOffset) {
				return new CellMatrixNumber(newHorizontalCellNumber, 0);
			}
			if (currentCellPosition.getyPos() <= y && nextCellPosition.getyPos() >= y) {
				newVerticalCellNumber = verticalCellNumber;
			}
		}
		return new CellMatrixNumber(newHorizontalCellNumber, newVerticalCellNumber);
	}

	private void insertWidgetinCellPosistion(Widget widget, CellMatrixNumber newCellNum) {

		Widget widgetToMov = cellWidgets[newCellNum.getX()][newCellNum.getY()];
		cellWidgets[newCellNum.getX()][newCellNum.getY()] = widget;

		CellPosition newPosition = cellPositions[newCellNum.getX()][newCellNum.getY()];
		positionWidget(widget, newPosition);

		if (widgetToMov != null) {
			insertWidgetinCellPosistion(widgetToMov, getNextCell(newCellNum.getX(), newCellNum.getY()));
		}

	}

	private void positionWidget(Widget widget, CellPosition newPosition) {
		final Style style = widget.getElement().getStyle();
		style.setPropertyPx("left", newPosition.getxPos());
		style.setPropertyPx("top", newPosition.getyPos());
		
	}

	private CellMatrixNumber getNextCell(int horizontalcellnumber, int verticalCellNumber) {
		if (horizontalcellnumber + 1 < numHorizontalCells) {
			return new CellMatrixNumber(horizontalcellnumber + 1, verticalCellNumber);
		} else {
			return new CellMatrixNumber(0, verticalCellNumber + 1);
		}

	}

	protected ArrayList<You2MixVideo> getVideoList() {
		ArrayList<You2MixVideo> widgets = new ArrayList<You2MixVideo>();
		for (int verticalCellNumber = 0; verticalCellNumber < numVerticalCells; verticalCellNumber++) {
			for (int horizontalcellnumber = 0; horizontalcellnumber < numHorizontalCells; horizontalcellnumber++) {
				if (cellWidgets[horizontalcellnumber][verticalCellNumber] != null) {
					You2MixVideoDataView videoDataView = (You2MixVideoDataView) cellWidgets[horizontalcellnumber][verticalCellNumber];

					widgets.add(videoDataView.getVideoData().getVideo());
				}

			}

		}
		return widgets;

	}

	public void setObserver(VideoMixPositionObserver observer) {
		this.observer = observer;
	}

	/**
	 * @param padHeight
	 *            the padHeight to set
	 */
	public void setPadHeight(int padHeight) {
		this.padHeight = padHeight;
	}

	/**
	 * @return the padHeight
	 */
	public int getPadHeight() {
		return padHeight;
	}

	/**
	 * @param padCellWidth
	 *            the padCellWidth to set
	 */
	public void setPadCellWidth(int padCellWidth) {
		this.padCellWidth = padCellWidth;
	}

	/**
	 * @return the padCellWidth
	 */
	public int getPadCellWidth() {
		return padCellWidth;
	}

	/**
	 * @param padWidth
	 *            the padWidth to set
	 */
	public void setPadWidth(int padWidth) {
		this.padWidth = padWidth;
	}

	/**
	 * @return the padWidth
	 */
	public int getPadWidth() {
		return padWidth;
	}

	/**
	 * @param padCellHeight
	 *            the padCellHeight to set
	 */
	public void setPadCellHeight(int padCellHeight) {
		this.padCellHeight = padCellHeight;
	}

	/**
	 * @return the padCellHeight
	 */
	public int getPadCellHeight() {
		return padCellHeight;
	}

	public class CellPosition {
		private int xPos;
		private int yPos;

		public CellPosition(int xPos, int yPos) {
			this.xPos = xPos;
			this.yPos = yPos;
		}

		public int getxPos() {
			return xPos;
		}

		public int getyPos() {
			return yPos;
		}

	}

	public class CellMatrixNumber {
		private int x;
		private int y;

		public CellMatrixNumber(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}
	}
}
