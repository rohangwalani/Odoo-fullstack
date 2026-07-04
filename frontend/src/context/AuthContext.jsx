import { createContext, useState, useEffect } from 'react';
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

  useEffect(() => {
    // Check if user is logged in
    const checkAuth = async () => {
      try {
        const token = localStorage.getItem('authToken'); // we'll use email as the token for now
        if (token) {
          const users = getMockUsers();
          const foundUser = users.find(u => u.email === token);
          if (foundUser) {
            setUser(foundUser);
          } else {
            localStorage.removeItem('authToken');
            localStorage.removeItem('userData');
          }
        } else {
          localStorage.removeItem('authToken');
          localStorage.removeItem('userData');
        }
      } catch (error) {
        console.error('Failed to parse user data:', error);
        localStorage.removeItem('authToken');
        localStorage.removeItem('userData');
      } finally {
        setLoading(false);
      }
    };

    checkAuth();
  }, []);

  const login = (token, userData) => {
    localStorage.setItem('authToken', token);
    localStorage.setItem('userData', JSON.stringify(userData));
    setUser(userData);
  };

  const logout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userData');
    setUser(null);
  };

  const registerUser = (userData) => {
    const users = getMockUsers();
    // Check if email exists
    if (users.find(u => u.email === userData.email)) {
      throw new Error('Email already registered');
    }
    
    // Assign mock backend fields
    const newUser = {
      ...userData,
      id: `EMP-${Math.floor(Math.random() * 9000) + 1000}`,
      status: 'present',
      department: userData.department || 'General',
      manager: 'Not Assigned',
      location: 'HQ',
      joiningDate: new Date().toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }),
      salary: '$60,000', // Default mock salary
      avatarUrl: `https://i.pravatar.cc/150?u=${userData.email}`, // Random consistent avatar
    };

    users.push(newUser);
    saveMockUsers(users);
    return newUser;
  };

  const authenticateUser = (email, password) => {
    const users = getMockUsers();
    const found = users.find(u => u.email.toLowerCase() === email.toLowerCase() && u.password === password);
    if (!found) {
      throw new Error('Invalid email or password');
    }
    return found;
  };

  const updateUser = (updatedFields) => {
    if (!user) throw new Error('No user logged in');
    
    const users = getMockUsers();
    const userIndex = users.findIndex(u => u.id === user.id);
    
    if (userIndex === -1) {
      throw new Error('User not found in database');
    }
    
    const updatedUser = { ...users[userIndex], ...updatedFields };
    users[userIndex] = updatedUser;
    
    saveMockUsers(users);
    setUser(updatedUser);
    
    return updatedUser;
  };

  const updateEmployee = (employeeId, updatedFields) => {
    const users = getMockUsers();
    let index = users.findIndex(u => u.id === employeeId);
    
    let targetUser;
    
    if (index === -1) {
      // If not in local storage yet, find in static mocks and clone it to local storage
      const staticMock = mockEmployees.find(m => m.id === employeeId || m.empId === employeeId);
      if (!staticMock) throw new Error('Employee not found in DB or mocks');
      
      targetUser = { ...staticMock, id: employeeId, ...updatedFields };
      users.push(targetUser);
      index = users.length - 1;
    } else {
      targetUser = { ...users[index], ...updatedFields };
      users[index] = targetUser;
    }
    
    saveMockUsers(users);
    
    // If the admin is updating their own profile via the employee page, update their session too
    if (user && user.id === employeeId) {
      setUser(targetUser);
    }
    
    return targetUser;
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
