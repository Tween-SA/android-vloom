package com.tween.viacelular.adapters;

import com.tween.viacelular.models.Message;
import java.util.Comparator;

/**
 * Created by davidfigueroa on 4/5/16.
 */
public class TimestampComparator implements Comparator<Message>
{
	@Override
	public int compare(Message o1, Message o2)
	{
		if(o2.getCreated() > o1.getCreated())
		{
			return 1;
		}
		else
		{
			if(o1.getCreated() > o2.getCreated())
			{
				return -1;
			}
			else
			{
				return 0;
			}
		}
	}
}