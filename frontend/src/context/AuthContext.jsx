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
      name: 'Sarah Jenkins',
      fullName: 'Sarah Jenkins',
      email: 'admin@corehr.com',
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
      name: 'Michael Chen',
      fullName: 'Michael Chen',
      email: 'employee@corehr.com',
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
          }
        }
      } catch (error) {
        console.error('Failed to verify token:', error);
        localStorage.removeItem('authToken');
      } finally {
        setLoading(false);
      }
    };

    checkAuth();
  }, []);

  const login = (token, userData) => {
    localStorage.setItem('authToken', token);
    setUser(userData);
  };

  const logout = () => {
    localStorage.removeItem('authToken');
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

  const value = {
    user,
    loading,
    login,
    logout,
    registerUser,
    authenticateUser,
    updateUser,
    updateEmployee,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
