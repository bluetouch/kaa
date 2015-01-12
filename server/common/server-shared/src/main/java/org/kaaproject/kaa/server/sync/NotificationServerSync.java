/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kaaproject.kaa.server.sync;  

import java.util.List;

public class NotificationServerSync {
   private int appStateSeqNumber;
   private SyncResponseStatus responseStatus;
   private List<Notification> notifications;
   private List<Topic> availableTopics;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}. 
   */
  public NotificationServerSync() {}

  /**
   * All-args constructor.
   */
  public NotificationServerSync(java.lang.Integer appStateSeqNumber, SyncResponseStatus responseStatus, List<Notification> notifications, List<Topic> availableTopics) {
    this.appStateSeqNumber = appStateSeqNumber;
    this.responseStatus = responseStatus;
    this.notifications = notifications;
    this.availableTopics = availableTopics;
  }

  /**
   * Gets the value of the 'appStateSeqNumber' field.
   */
  public java.lang.Integer getAppStateSeqNumber() {
    return appStateSeqNumber;
  }

  /**
   * Sets the value of the 'appStateSeqNumber' field.
   * @param value the value to set.
   */
  public void setAppStateSeqNumber(java.lang.Integer value) {
    this.appStateSeqNumber = value;
  }

  /**
   * Gets the value of the 'responseStatus' field.
   */
  public SyncResponseStatus getResponseStatus() {
    return responseStatus;
  }

  /**
   * Sets the value of the 'responseStatus' field.
   * @param value the value to set.
   */
  public void setResponseStatus(SyncResponseStatus value) {
    this.responseStatus = value;
  }

  /**
   * Gets the value of the 'notifications' field.
   */
  public List<Notification> getNotifications() {
    return notifications;
  }

  /**
   * Sets the value of the 'notifications' field.
   * @param value the value to set.
   */
  public void setNotifications(List<Notification> value) {
    this.notifications = value;
  }

  /**
   * Gets the value of the 'availableTopics' field.
   */
  public List<Topic> getAvailableTopics() {
    return availableTopics;
  }

  /**
   * Sets the value of the 'availableTopics' field.
   * @param value the value to set.
   */
  public void setAvailableTopics(List<Topic> value) {
    this.availableTopics = value;
  }

@Override
public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("NotificationServerSync [appStateSeqNumber=");
    builder.append(appStateSeqNumber);
    builder.append(", responseStatus=");
    builder.append(responseStatus);
    builder.append(", notifications=");
    builder.append(notifications);
    builder.append(", availableTopics=");
    builder.append(availableTopics);
    builder.append("]");
    return builder.toString();
}
}
