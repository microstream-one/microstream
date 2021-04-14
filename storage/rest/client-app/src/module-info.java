module microstream.storage.restclient.app
{
	exports one.microstream.storage.restclient.app.types;
	exports one.microstream.storage.restclient.app.ui;
	
	requires flow.data;
	requires flow.html.components;
	requires flow.server;
	requires gwt.elemental;
	requires microstream.base;
	requires microstream.storage.restadapter;
	requires microstream.storage.restclient;
	requires microstream.storage.restclient.jersey;
	requires org.apache.tomcat.embed.core;
	requires org.slf4j;
	requires spring.beans;
	requires spring.boot;
	requires spring.boot.autoconfigure;
	requires spring.context;
	requires spring.core;
	requires spring.web;
	requires vaadin.button.flow;
	requires vaadin.combo.box.flow;
	requires vaadin.details.flow;
	requires vaadin.grid.flow;
	requires vaadin.lumo.theme;
	requires vaadin.notification.flow;
	requires vaadin.ordered.layout.flow;
	requires vaadin.split.layout.flow;
	requires vaadin.tabs.flow;
	requires vaadin.text.field.flow;
}
