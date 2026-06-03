package com.example.yarsi.student.pawlink.data.repository

import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.Session
import com.example.yarsi.student.pawlink.config.AppWriteProvider

class AuthRepository {

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<String> {

        return try {

            AppWriteProvider.account.create(
                userId = ID.unique(),
                email = email,
                password = password,
                name = name
            )

            Result.success("Register berhasil")

        } catch (e: AppwriteException) {

            Result.failure(Exception(e.message))

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<Session> {

        return try {

            val session = AppWriteProvider.account.createEmailPasswordSession(
                email = email,
                password = password
            )

            Result.success(session)

        } catch (e: AppwriteException) {

            Result.failure(Exception(e.message))

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<String> {
        return try {
            val user = AppWriteProvider.account.get()
            Result.success(user.name)
        } catch (e: AppwriteException) {
            Result.failure(Exception(e.message))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<String> {

        return try {

            AppWriteProvider.account.deleteSession("current")

            Result.success("Logout berhasil")

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}