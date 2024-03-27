import android.content.Context
import android.net.ConnectivityManager
import android.util.Patterns

object ValidationUtils {
    fun validateFieldsSignUp(email: String, username: String, firstName: String, password: String): Any {
        return when {
            isAnyFieldEmpty(email, username, firstName, password) -> ValidationResult.EMPTY_FIELD
            containsWhiteSpace(email, username, password) -> ValidationResult.WHITESPACE_IN_FIELD
            !isEmailValid(email) -> ValidationResult.INVALID_EMAIL
            containsNumbers(firstName) -> ValidationResult.INVALID_NAME
            !isPasswordStrongEnough(password) -> ValidationResult.WEAK_PASSWORD
            !isValidFirstName(firstName) -> ValidationResult.INVALID_NAME
            else -> ValidationResult.SUCCESS
        }
    }

    fun validateFieldsLogIn(username: String, password: String): Any {
        return when {
            isAnyFieldEmpty(username, password) -> ValidationResult.EMPTY_FIELD
            containsWhiteSpace(username, password) -> ValidationResult.WHITESPACE_IN_FIELD
            else -> ValidationResult.SUCCESS
        }
    }

    private fun containsWhiteSpace(vararg fields: String): Boolean {
        return fields.any { it.contains(" ") }
    }

    private fun isEmailValid(email: String): Boolean {
        val emailPattern = Patterns.EMAIL_ADDRESS
        return emailPattern.matcher(email).matches()
    }

    private fun isAnyFieldEmpty(vararg fields: String): Boolean {
        return fields.any { it.isBlank() }
    }

    private fun containsNumbers(vararg fields: String): Boolean {
        val regex = Regex("\\d")
        return fields.any { field ->
            regex.containsMatchIn(field)
        }
    }

    private fun isPasswordStrongEnough(password: String): Boolean {
        val digitRegex = Regex("\\d")
        val upperCaseRegex = Regex("[A-Z]")
        val lowerCaseRegex = Regex("[a-z]")
        val specialCharacterRegex = Regex("[^A-Za-z0-9]")

        return password.length >= 4 &&
                digitRegex.containsMatchIn(password) &&
                upperCaseRegex.containsMatchIn(password) &&
                lowerCaseRegex.containsMatchIn(password) &&
                specialCharacterRegex.containsMatchIn(password) &&
                !password.contains(" ")
    }

    private fun isValidFirstName(firstName: String): Boolean {
        val nameParts = firstName.split(" ")

        // Si solo hay un nombre, debe estar presente y no debe haber espacios adicionales al final
        if (nameParts.size == 1) {
            return nameParts[0].isNotBlank() && !nameParts[0].endsWith(" ")
        }

        // Si hay dos nombres, ambos deben estar presentes y ninguno debe estar en blanco
        if (nameParts.size == 2) {
            return nameParts[0].isNotBlank() && nameParts[1].isNotBlank()
        }

        return false
    }

    fun showInvalidFieldsAlert(context: Context, validationResult: Any) {
        val alertDialogBuilder = androidx.appcompat.app.AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("Invalid Fields")

        val errorMessage = when (validationResult) {
            ValidationResult.EMPTY_FIELD -> "Please fill in all fields."
            ValidationResult.INVALID_EMAIL -> "Please enter a valid email address."
            ValidationResult.INVALID_NAME -> "Please enter a valid first name with at most two names separated by a single space."
            ValidationResult.PASSWORDS_NOT_MATCH -> "The passwords entered do not match."
            ValidationResult.WEAK_PASSWORD -> "The password does not meet the minimum requirements: it must be at least 4 characters long and contain at least one digit, one uppercase letter, one lowercase letter, and one special character."
            ValidationResult.WHITESPACE_IN_FIELD -> "One or more fields contain whitespace. Please remove any leading or trailing spaces."
            else -> "Please check your entries."
        }

        alertDialogBuilder.setMessage(errorMessage)
        alertDialogBuilder.setPositiveButton("OK") { _, _ ->
            // Dismiss the alert dialog if OK is pressed
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    enum class ValidationResult {
        SUCCESS,
        INVALID_EMAIL,
        EMPTY_FIELD,
        INVALID_NAME,
        PASSWORDS_NOT_MATCH,
        WEAK_PASSWORD,
        WHITESPACE_IN_FIELD
    }

    @Deprecated("Deprecated in Java")
    fun isConnect(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}
