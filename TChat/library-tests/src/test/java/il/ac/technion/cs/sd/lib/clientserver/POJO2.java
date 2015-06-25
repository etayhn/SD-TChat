package il.ac.technion.cs.sd.lib.clientserver;

import java.util.List;


public class POJO2
{
	public int i;
	public String str;

	POJO2(int i, String str, List<POJO1> pojos) {
		this.i = i;
		this.str = str;
		this.pojos = pojos;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		POJO2 other = (POJO2) obj;
		if (i != other.i)
			return false;
		if (pojos == null) {
			if (other.pojos != null)
				return false;
		} else if (!pojos.equals(other.pojos))
			return false;
		if (str == null) {
			if (other.str != null)
				return false;
		} else if (!str.equals(other.str))
			return false;
		return true;
	}
	public List<POJO1> pojos;
}
