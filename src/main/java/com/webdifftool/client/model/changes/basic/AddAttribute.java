/*
 *
 *  * Copyright © 2014 - 2021 Leipzig University (Database Research Group)
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, version 3.
 *  *
 *  * This program is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.webdifftool.client.model.changes.basic;

import java.util.List;

public class AddAttribute extends BasicChange {

	public AddAttribute() {
		super();
	}
	
	public AddAttribute(String id, String name, List<String[]> changeValues) {
		super(id,name,changeValues);
	}
	
	public String getSimpleHTMLRepresenation()
	{
		String result = "<FONT COLOR=\"#800080\"><b>" + this.name + "</b></FONT>(";
		String concept = values.get(0)[0];
		String attributeName = values.get(1)[0];
		String value = values.get(2)[0];

		result += "<i " + this.getNameAsToolTip(concept) + ">" + concept + "</i>, ";
		result += "<b>" + attributeName + "</b>, ";
		result += "<FONT COLOR=\"#008000\">" + value + "</FONT> )";

		return result;
	}

}
