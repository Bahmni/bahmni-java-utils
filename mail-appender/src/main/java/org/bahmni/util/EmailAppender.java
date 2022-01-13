package org.bahmni.util;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import java.io.Serializable;

@Deprecated
public class EmailAppender extends AbstractAppender {
	protected EmailAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
		super(name, filter, layout);
	}
	@Override
	public void append(LogEvent event) {
	}
}
