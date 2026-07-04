import { createContext, useState, useEffect } from 'react';
import axiosInstance from '../api/axiosInstance';
import toast from 'react-hot-toast';
import { mockEmployees } from '../data/mockEmployees';

// Mock backend database helpers
const getMockUsers = () => {
  const users = localStorage.getItem('mock_db_users');
  if (users) {
    return JSON.parse(users);
  }
  
  // Seed default users if local storage is empty
  const defaultUsers = [
    {
      id: 'EMP-001',
      empId: 'EMP-001',
      name: 'Admin User',
      fullName: 'Sarah Jenkins',
      email: 'admin@hris.com',
      password: 'admin',
      role: 'Admin',
      status: 'present',
      department: 'Human Resources',
      manager: 'CEO',
      location: 'New York HQ',
      joiningDate: 'Jan 10, 2022',
      salary: '$95,000',
      avatarUrl: 'https://i.pravatar.cc/150?u=EMP-001'
    },
    {
      id: 'EMP-002',
      empId: 'EMP-002',
      name: 'Test Employee',
      fullName: 'Michael Chen',
      email: 'employee@hris.com',
      password: 'password',
      role: 'Employee',
      status: 'present',
      department: 'Engineering',
      manager: 'Sarah Jenkins',
      location: 'San Francisco',
      joiningDate: 'Mar 15, 2023',
      salary: '$120,000',
      avatarUrl: 'https://i.pravatar.cc/150?u=EMP-002'
    }
  ];
  localStorage.setItem('mock_db_users', JSON.stringify(defaultUsers));
  return defaultUsers;
};

const saveMockUsers = (users) => {
  localStorage.setItem('mock_db_users', JSON.stringify(users));
};

const getMockAttendance = () => {
  const att = localStorage.getItem('mock_db_attendance');
  if (att) return JSON.parse(att);
  return {};
};

const saveMockAttendance = (data) => {
  localStorage.setItem('mock_db_attendance', JSON.stringify(data));
};

const getMockLeaves = () => {
  const leaves = localStorage.getItem('mock_db_leaves');
  if (leaves) return JSON.parse(leaves);
  return []; // Array of leave objects
};

const saveMockLeaves = (data) => {
  localStorage.setItem('mock_db_leaves', JSON.stringify(data));
};

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

  const checkIn = () => {
    if (!user) return;
    const att = getMockAttendance();
    const today = new Date().toISOString().split('T')[0];
    
    if (!att[user.id]) att[user.id] = {};
    if (!att[user.id][today]) {
      att[user.id][today] = {
        date: today,
        checkInTime: new Date().toISOString(),
        checkOutTime: null,
      };
      saveMockAttendance(att);
    }
  };

  const checkOut = () => {
    if (!user) return;
    const att = getMockAttendance();
    const today = new Date().toISOString().split('T')[0];
    
    if (att[user.id] && att[user.id][today] && !att[user.id][today].checkOutTime) {
      att[user.id][today].checkOutTime = new Date().toISOString();
      saveMockAttendance(att);
    }
  };

  const getAttendanceForUser = (userId) => {
    const att = getMockAttendance();
    return att[userId] || {};
  };
  
  const getAllAttendance = () => {
    return getMockAttendance();
  };

  const isCheckedInToday = () => {
    if (!user) return false;
    const att = getMockAttendance();
    const today = new Date().toISOString().split('T')[0];
    const todayRecord = att[user.id]?.[today];
    return todayRecord && !todayRecord.checkOutTime;
  };

  const getAllLeaves = () => {
    return getMockLeaves();
  };

  const getLeavesForUser = (userId) => {
    const leaves = getMockLeaves();
    return leaves.filter(l => String(l.userId) === String(userId));
  };

  const requestLeave = (leaveData) => {
    if (!user) return;
    const leaves = getMockLeaves();
    const newLeave = {
      ...leaveData,
      id: `leave_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      userId: user.id,
      userName: user.name,
      status: 'Pending',
      createdAt: new Date().toISOString()
    };
    leaves.push(newLeave);
    saveMockLeaves(leaves);
    return newLeave;
  };

  const updateLeaveStatus = (leaveId, newStatus) => {
    const leaves = getMockLeaves();
    const leaveIndex = leaves.findIndex(l => l.id === leaveId);
    if (leaveIndex !== -1) {
      leaves[leaveIndex].status = newStatus;
      saveMockLeaves(leaves);
    }
  };

  const value = {
    user,
    loading,
    login,
    logout,
    registerUser,
    authenticateUser,
    updateUser,
    updateEmployee,
    checkIn,
    checkOut,
    isCheckedInToday,
    getAttendanceForUser,
    getAllAttendance,
    getAllLeaves,
    getLeavesForUser,
    requestLeave,
    updateLeaveStatus,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
