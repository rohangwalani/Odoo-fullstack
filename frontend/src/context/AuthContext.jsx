import { createContext, useState, useEffect } from 'react';
import axiosInstance from '../api/axiosInstance';
import toast from 'react-hot-toast';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Helper to map backend profile response to frontend user state format
  const mapBackendToUser = (profileData) => {
    // Determine role based on what the backend might return or infer from department/designation if missing
    // In our backend, Employee entity has a role enum: ADMIN, HR, EMPLOYEE
    // The profile endpoint should ideally return role, but if not we can just default to Employee
    return {
      id: profileData.employeeId,
      empId: profileData.employeeCode,
      name: `${profileData.firstName} ${profileData.lastName}`,
      fullName: `${profileData.firstName} ${profileData.lastName}`,
      email: profileData.email,
      role: profileData.role || 'Employee', // Backend might need to return this
      status: 'present', // mock status for UI
      department: profileData.department || 'General',
      designation: profileData.designation,
      location: 'HQ', // default
      joiningDate: profileData.joiningDate,
      avatarUrl: profileData.avatar ? `http://localhost:8080/uploads/${profileData.avatar}` : null,
      ...profileData // keep raw data
    };
  };

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const token = localStorage.getItem('authToken');
        if (token) {
          const response = await axiosInstance.get('/profile');
          setUser(mapBackendToUser(response.data));
        }
      } catch (error) {
        console.error('Failed to authenticate:', error);
        localStorage.removeItem('authToken');
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    checkAuth();
  }, []);

  const login = async (token, userData) => {
    // If the caller already passes token, we just set it
    // Usually, the caller does the POST /auth/login and gives us the token
    localStorage.setItem('authToken', token);
    
    // Fetch profile immediately after setting token
    try {
      const response = await axiosInstance.get('/profile');
      setUser(mapBackendToUser(response.data));
    } catch (error) {
      console.error("Failed to load profile after login", error);
    }
  };

  const logout = () => {
    localStorage.removeItem('authToken');
    setUser(null);
  };

  const registerUser = async (userData) => {
    try {
      // Create company / admin
      const response = await axiosInstance.post('/auth/company/signup', {
        companyName: userData.company,
        firstName: userData.name.split(' ')[0],
        lastName: userData.name.split(' ').slice(1).join(' ') || 'User',
        email: userData.email,
        phone: userData.phone,
        password: userData.password
      });
      
      return response.data;
    } catch (error) {
      throw error;
    }
  };

  const authenticateUser = async (email, password) => {
    try {
      const response = await axiosInstance.post('/auth/login', {
        email,
        password
      });
      return response.data; // { success, token, message }
    } catch (error) {
      throw error;
    }
  };

  const updateUser = (updatedFields) => {
    // Optimistic update for UI
    setUser(prev => ({ ...prev, ...updatedFields }));
  };

  const value = {
    user,
    loading,
    login,
    logout,
    registerUser,
    authenticateUser,
    updateUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
