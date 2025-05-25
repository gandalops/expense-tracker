import axios from "axios";
import API_BASE_URL from "./auth.config";

/**
 * Service for handling all authentication-related API calls
 * Note: All sensitive data handling should be done server-side
 */

// ======================== AUTHENTICATION METHODS ========================

/**
 * Register a new user
 * @param {string} username - User's display name
 * @param {string} email - User's login email
 * @param {string} password - Plain text password (hashed server-side)
 * @returns {Promise} Axios response
 */
const register_req = async (username, email, password) => {
  return await axios.post(`${API_BASE_URL}/auth/signup`, {
    userName: username,
    email: email,
    password: password // Backend will hash this
  });
};

/**
 * Authenticate user and store JWT token
 * @param {string} email - Registered email
 * @param {string} password - User's password
 * @returns {Promise} Axios response with token
 */
const login_req = async (email, password) => {
  try {
    const response = await axios.post(
      `${API_BASE_URL}/auth/signin`,
      { email, password },
      {
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        withCredentials: true
      }
    );

    if (response.data?.token) {
      const safeUserData = {
        token: response.data.token,
        email: response.data.email,
        roles: response.data.roles,
        username: response.data.username,
        id: response.data.id
      };
      localStorage.setItem("user", JSON.stringify(safeUserData));
    }
    return response;
  } catch (error) {
    console.error("Login failed:", error.response?.data || error.message);
    throw error;
  }
};

// ======================== VERIFICATION METHODS ========================

/**
 * Verify user registration with code
 * @param {string} verificationCode - Code sent via email
 * @returns {Promise} Axios response
 */
const verifyRegistrationVerificationCode = async (verificationCode) => {
  return await axios.get(`${API_BASE_URL}/auth/signup/verify`, {
    params: { code: verificationCode }
  });
};

/**
 * Resend verification code
 * @param {string} email - User's email
 * @returns {Promise} Axios response
 */
const resendRegistrationVerificationCode = async (email) => {
  return await axios.get(`${API_BASE_URL}/auth/signup/resend`, {
    params: { email }
  });
};

// ======================== PASSWORD METHODS ========================

/**
 * Initiate password reset
 * @param {string} email - User's registered email
 * @returns {Promise} Axios response
 */
const forgotPasswordVerifyEmail = async (email) => {
  return await axios.get(`${API_BASE_URL}/auth/forgotPassword/verifyEmail`, {
    params: { email }
  });
};

/**
 * Verify password reset code
 * @param {string} code - Reset code from email
 * @returns {Promise} Axios response
 */
const forgotPasswordverifyCode = async (code) => {
  return await axios.get(`${API_BASE_URL}/auth/forgotPassword/verifyCode`, {
    params: { code }
  });
};

/**
 * Resend password reset code
 * @param {string} email - User's email
 * @returns {Promise} Axios response
 */
const resendResetPasswordVerificationCode = async (email) => {
  return await axios.get(`${API_BASE_URL}/auth/forgotPassword/resendEmail`, {
    params: { email }
  });
};

/**
 * Submit new password
 * @param {string} email - User's email
 * @param {string} password - New password
 * @returns {Promise} Axios response
 */
const resetPassword = async (email, password) => {
  return await axios.post(`${API_BASE_URL}/auth/forgotPassword/resetPassword`, {
    email,
    newPassword: password
  });
};

// ======================== UTILITY METHODS ========================

/**
 * Get current user from localStorage
 * @returns {Object|null} Parsed user object or null
 */
const getCurrentUser = () => {
  const user = localStorage.getItem("user");
  return user ? JSON.parse(user) : null;
};

/**
 * Remove user data on logout
 */
const logout_req = () => {
  localStorage.removeItem("user");
};

/**
 * Generate auth header for authenticated requests
 * @returns {Object} Authorization header or empty object
 */
const authHeader = () => {
  const user = getCurrentUser();
  return user?.token ? { Authorization: 'Bearer ' + user.token } : {};
};

// ======================== SERVICE EXPORT ========================

const AuthService = {
  register_req,
  login_req,
  verifyRegistrationVerificationCode,
  resendRegistrationVerificationCode,
  getCurrentUser,
  logout_req,
  forgotPasswordVerifyEmail,
  forgotPasswordverifyCode,
  resendResetPasswordVerificationCode,
  resetPassword,
  authHeader
};

export default AuthService;