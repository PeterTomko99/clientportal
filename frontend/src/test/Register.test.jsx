import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { vi } from 'vitest';
import Register from '../pages/Register';
import api from '../api';

vi.mock('../api');
vi.mock('react-hot-toast', () => ({
  default: { success: vi.fn(), error: vi.fn() },
}));

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal();
  return { ...actual, useNavigate: () => mockNavigate };
});

describe('Register', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  function setup() {
    render(
      <MemoryRouter>
        <Register />
      </MemoryRouter>
    );
  }

  it('renders all fields without a role selector', () => {
    setup();
    expect(screen.getByLabelText(/full name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.queryByLabelText(/role/i)).not.toBeInTheDocument();
  });

  it('navigates to login on successful registration', async () => {
    api.post.mockResolvedValue({});
    setup();

    await userEvent.type(screen.getByLabelText(/full name/i), 'Jan Novak');
    await userEvent.type(screen.getByLabelText(/email/i), 'jan@example.com');
    await userEvent.type(screen.getByLabelText(/password/i), 'password123');
    await userEvent.click(screen.getByRole('button', { name: /create account/i }));

    await waitFor(() => expect(mockNavigate).toHaveBeenCalledWith('/login'));
  });

  it('shows error toast on 409 conflict', async () => {
    const toast = (await import('react-hot-toast')).default;
    api.post.mockRejectedValue({ response: { status: 409 } });
    setup();

    await userEvent.type(screen.getByLabelText(/full name/i), 'Jan Novak');
    await userEvent.type(screen.getByLabelText(/email/i), 'jan@example.com');
    await userEvent.type(screen.getByLabelText(/password/i), 'password123');
    await userEvent.click(screen.getByRole('button', { name: /create account/i }));

    await waitFor(() =>
      expect(toast.error).toHaveBeenCalledWith('An account with that email already exists.')
    );
  });
});
