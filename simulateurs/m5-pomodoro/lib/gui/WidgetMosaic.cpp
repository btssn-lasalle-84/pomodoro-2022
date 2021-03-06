#include "WidgetMosaic.h"
using namespace Codingfield::UI;

WidgetMosaic::WidgetMosaic(Widget* parent, Point position, Size size, int32_t nbColumns, int32_t nbRows) : Widget(parent, position, size),
                                                                                                           nbColumns {nbColumns},
                                                                                                           nbRows {nbRows} {

}

WidgetMosaic::WidgetMosaic(int32_t nbColumns, int32_t nbRows) : WidgetMosaic(nullptr, Point(), Size(), nbColumns, nbRows) {

}

Color WidgetMosaic::GetBackgroundColor() {
    return WHITE;
}

void WidgetMosaic::Draw() {
  if(IsHidden()) return;
  if(isInvalidated) {
    M5.Lcd.fillRect(position.x, position.y, size.width, size.height, BLACK);
  }

  for(Widget* w : children)
    w->Draw();

  isInvalidated = false;
}

void WidgetMosaic::AddChild(Widget* widget) {
  Widget::AddChild(widget);

  Size widgetSize = ComputeWidgetSize();
  widget->SetSize(widgetSize);

  int32_t position = ((children.size()-1) % (nbRows*nbColumns));
  widget->SetPosition(ComputeWidgetPosition(widgetSize, position));
  Invalidate();
}

const Widget* WidgetMosaic::GetSelected() const {
  return selectedWidget;
}

void WidgetMosaic::ZoomOnSelected(bool enabled) {
  bool oldValue = zoomOnSelected;
  if(selectedWidget != nullptr && enabled) {
    for(auto* w : children)
      w->Hide();

    zoomOnSelected = true;
    Size widgetSize = ComputeWidgetSize(1,1);
    selectedWidget->SetSize(widgetSize);
    selectedWidget->SetPosition(ComputeWidgetPosition(widgetSize, 0));
    selectedWidget->Show();
    selectedWidget->Invalidate();
    if(selectedWidget->IsEditable()) {
      selectedWidget->EnableControls();
    }
  }  else {
    zoomOnSelected = false;

    for(auto index = 0; index < children.size(); index++) {
      Widget* widget = children.at(index);
      Size widgetSize = ComputeWidgetSize();
      widget->SetSize(widgetSize);
      widget->SetPosition(ComputeWidgetPosition(widgetSize, index));
      widget->Show();
      if(selectedWidget->IsEditable()) {
        selectedWidget->DisableControls();
      }
    }
  }

  if(selectedWidget != nullptr && oldValue != zoomOnSelected) {
    zoomOnSelectedCallback(selectedWidget, zoomOnSelected);
    Invalidate();
  }
}

void WidgetMosaic::OnButtonAPressed() {
  if(zoomOnSelected)
    return selectedWidget->OnButtonAPressed();

  if(indexSelected > 0) {
    children[indexSelected]->SetSelected(false);
    indexSelected--;
    children[indexSelected]->SetSelected(true);
    selectedWidget = children[indexSelected];
  }
}

void WidgetMosaic::OnButtonBPressed() {
  if(selectedWidget == nullptr) return;
  if(zoomOnSelected) {
    ZoomOnSelected(false);
    selectedWidget->OnButtonBPressed();
    return;
  }

  ZoomOnSelected(true);
}

void WidgetMosaic::OnButtonBLongPush() {
  if(zoomOnSelected)
    selectedWidget->OnButtonBLongPush();
  ZoomOnSelected(false);
}

void WidgetMosaic::OnButtonCPressed() {
  if(zoomOnSelected)
    return selectedWidget->OnButtonCPressed();

  if(indexSelected < children.size()-1) {
    children[indexSelected]->SetSelected(false);
    indexSelected++;
    children[indexSelected]->SetSelected(true);
    selectedWidget = children[indexSelected];
  }
}

Size WidgetMosaic::ComputeWidgetSize() {
  return ComputeWidgetSize(nbColumns, nbRows);
}

Size WidgetMosaic::ComputeWidgetSize(int32_t nbColumns, int32_t nbRows) {
  Size widgetSize;
  widgetSize.width = (size.width - ((2*border) + ((nbColumns-1)*border))) / nbColumns;
  widgetSize.height = (size.height - ((2*border) + ((nbRows-1)*border))) / nbRows;
  return widgetSize;
}

Point WidgetMosaic::ComputeWidgetPosition(const Size& widgetSize, int32_t position) {
  Point widgetPosition;

  int32_t row = ((int32_t)position / (int32_t)nbColumns);
  int32_t column = position % nbColumns;
  widgetPosition.x = ((column)*border) + (column * widgetSize.width) + this->position.x;
  widgetPosition.y = ((row)*border) + (row * widgetSize.height) + this->position.y;

  return widgetPosition;
}

bool WidgetMosaic::IsZoomOnSelected() const {
  return zoomOnSelected;
}

void WidgetMosaic::SetZoomOnSelectedCallback(std::function<void (Widget* widget, bool)>func) {
  zoomOnSelectedCallback = func;
}

void WidgetMosaic::Run() {
    Draw();
    for(Widget* w : children)
        w->Run();
}
