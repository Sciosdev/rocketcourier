import com.rocket.service.utils.PasswordStorage;
import com.rocket.service.utils.PasswordStorage.CannotPerformOperationException;

public class Test {

	public static void main(String[] args) throws CannotPerformOperationException {

		String password = PasswordStorage.createHash("admin");
		System.out.println(password);

	}
}