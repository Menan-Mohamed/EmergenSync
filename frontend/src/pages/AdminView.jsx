import { useState, useEffect, useCallback } from 'react';
import Paper from '@mui/material/Paper';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TablePagination from '@mui/material/TablePagination';
import TableRow from '@mui/material/TableRow';
import TextField from '@mui/material/TextField';
import MenuItem from '@mui/material/MenuItem';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import Checkbox from '@mui/material/Checkbox';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import Box from '@mui/material/Box';
import CircularProgress from '@mui/material/CircularProgress';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';

const columns = [
  { id: 'select', label: '', minWidth: 40 },
  { id: 'id', label: 'ID', minWidth: 70 },
  { id: 'username', label: 'Username', minWidth: 150 },
  { id: 'type', label: 'Type', minWidth: 100 },
  { id: 'role', label: 'Role', minWidth: 100 },
  { id: 'actions', label: 'Actions', minWidth: 100, align: 'center' },
];

export default function AdminView() {
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [users, setUsers] = useState([]);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(false);

  // Filters
  const [search, setSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [roleFilter, setRoleFilter] = useState('');

  // Selected rows
  const [selected, setSelected] = useState([]);

  // Add User Modal
  const [openAddModal, setOpenAddModal] = useState(false);
  const [newUser, setNewUser] = useState({
    username: '',
    password: '',
    type: '',
    role: ''
  });

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    try {
      // Build criteria object - only include non-empty values
      const criteria = {};
      
      if (search && search.trim()) {
        criteria.search = search.trim();
      }
      
      if (typeFilter) {
        criteria.type = typeFilter; // Send as string, Spring will convert to enum
      }
      
      if (roleFilter) {
        criteria.role = roleFilter; // Send as string, Spring will convert to enum
      }

      console.log('Sending criteria:', criteria); // Debug what we're sending

      const response = await fetch(
        `http://localhost:8080/api/users/filter?page=${page}&size=${rowsPerPage}`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(criteria),
        }
      );

      console.log('Response status:', response.status); // Debug response status

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Error response:', errorText);
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      console.log('Received full data:', data);
      console.log('Content array:', data.content);
      console.log('Total elements:', data.totalElements);
      
      setUsers(data.content || []);
      setTotalElements(data.totalElements || 0);
    } catch (error) {
      console.error('Error fetching users:', error);
      alert('Error loading users. Check console for details.');
    } finally {
      setLoading(false);
    }
  }, [search, typeFilter, roleFilter, page, rowsPerPage]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  // Pagination handlers
  const handleChangePage = (_, newPage) => setPage(newPage);
  const handleChangeRowsPerPage = (e) => {
    setRowsPerPage(+e.target.value);
    setPage(0);
  };

  // Filter Reset
  const handleReset = () => {
    setSearch('');
    setTypeFilter('');
    setRoleFilter('');
    setPage(0);
    fetchUsers();
  };

  // Single Delete
  const handleDelete = async (id) => {
    if (!window.confirm('Delete this user?')) return;

    try {
      await fetch(`http://localhost:8080/api/users/${id}`, { method: 'DELETE' });
      fetchUsers();
    } catch (err) {
      console.error('Delete error:', err);
    }
  };

  // Multi Delete
  const handleDeleteSelected = async () => {
    if (selected.length === 0) return;

    if (!window.confirm(`Delete ${selected.length} users?`)) return;

    for (const id of selected) {
      await fetch(`http://localhost:8080/api/users/${id}`, { method: 'DELETE' });
    }

    setSelected([]);
    fetchUsers();
  };

  // Checkbox toggle
  const toggleSelect = (id) => {
    setSelected((prev) =>
      prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]
    );
  };

  // Add user
  const handleAddUser = async () => {
    try {
      await fetch(`http://localhost:8080/api/auth/signup`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newUser),
      });

      setOpenAddModal(false);
      setNewUser({ username: '', password: '', type: '', role: '' });
      fetchUsers();
    } catch (err) {
      console.error('Add user error:', err);
    }
  };

  return (
    <Paper sx={{ width: '100%', p: 2 }}>
      {/* Filters and Actions */}
      <Box sx={{ mb: 3, display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
        
        <TextField
          label="Search"
          size="small"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          sx={{ minWidth: 180 }}
        />

        <TextField
          select
          label="Type"
          size="small"
          value={typeFilter}
          onChange={(e) => setTypeFilter(e.target.value)}
          sx={{ minWidth: 120 }}
        >
          <MenuItem value="">All</MenuItem>
          <MenuItem value="MEDICAL">Medical</MenuItem>
          <MenuItem value="FIRE">Fire</MenuItem>
          <MenuItem value="POLICE">Police</MenuItem>
        </TextField>

        <TextField
          select
          label="Role"
          size="small"
          value={roleFilter}
          onChange={(e) => setRoleFilter(e.target.value)}
          sx={{ minWidth: 130 }}
        >
          <MenuItem value="">All</MenuItem>
          <MenuItem value="DISPATCHER">Dispatcher</MenuItem>
          <MenuItem value="EMERGENCY_RESPONDER">Responder</MenuItem>
        </TextField>

        <Button variant="contained" onClick={fetchUsers}>Search</Button>
        <Button variant="outlined" onClick={handleReset}>Reset</Button>

        <Button
          variant="contained"
          color="success"
          startIcon={<AddIcon />}
          onClick={() => setOpenAddModal(true)}
        >
          Add User
        </Button>

        <Button
          variant="outlined"
          color="error"
          disabled={selected.length === 0}
          startIcon={<DeleteIcon />}
          onClick={handleDeleteSelected}
        >
          Delete Selected
        </Button>
      </Box>

      {/* Table */}
      <TableContainer sx={{ maxHeight: 440 }}>
        <Table stickyHeader>
          <TableHead>
            <TableRow>
              {columns.map((col) => (
                <TableCell key={col.id} align={col.align} style={{ fontWeight: 'bold' }}>
                  {col.label}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>

          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={columns.length} align="center">
                  <CircularProgress />
                </TableCell>
              </TableRow>
            ) : users.length === 0 ? (
              <TableRow>
                <TableCell colSpan={columns.length} align="center">
                  No users found
                </TableCell>
              </TableRow>
            ) : (
              users.map((user) => (
                <TableRow hover key={user.id}>
                  <TableCell>
                    <Checkbox
                      checked={selected.includes(user.id)}
                      onChange={() => toggleSelect(user.id)}
                    />
                  </TableCell>

                  <TableCell>{user.id}</TableCell>
                  <TableCell>{user.username}</TableCell>
                  <TableCell>{user.type}</TableCell>
                  <TableCell>{user.role}</TableCell>

                  <TableCell align="center">
                    <IconButton color="error" onClick={() => handleDelete(user.id)}>
                      <DeleteIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <TablePagination
        rowsPerPageOptions={[10, 25, 50, 100]}
        component="div"
        count={totalElements}
        rowsPerPage={rowsPerPage}
        page={page}
        onPageChange={handleChangePage}
        onRowsPerPageChange={handleChangeRowsPerPage}
      />

      {/* ADD USER MODAL */}
      <Dialog open={openAddModal} onClose={() => setOpenAddModal(false)}>
        <DialogTitle>Add User</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
          
          <TextField
            label="Username"
            value={newUser.username}
            onChange={(e) => setNewUser({ ...newUser, username: e.target.value })}
          />

          <TextField
            label="Password"
            type="password"
            value={newUser.password}
            onChange={(e) => setNewUser({ ...newUser, password: e.target.value })}
          />

          <TextField
            select
            label="Type"
            value={newUser.type}
            onChange={(e) => setNewUser({ ...newUser, type: e.target.value })}
          >
            <MenuItem value="MEDICAL">Medical</MenuItem>
            <MenuItem value="FIRE">Fire</MenuItem>
            <MenuItem value="POLICE">Police</MenuItem>
          </TextField>

          <TextField
            select
            label="Role"
            value={newUser.role}
            onChange={(e) => setNewUser({ ...newUser, role: e.target.value })}
          >
            <MenuItem value="DISPATCHER">Dispatcher</MenuItem>
            <MenuItem value="EMERGENCY_RESPONDER">Responder</MenuItem>
          </TextField>
        </DialogContent>

        <DialogActions>
          <Button onClick={() => setOpenAddModal(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleAddUser}>Add</Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
}
