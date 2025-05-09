import '@testing-library/jest-dom';
import { afterAll, afterEach, beforeAll } from 'vitest';
import { server } from './testServer';

// Establish API mocking before all tests.
beforeAll(() => server.listen());
// Reset any request handlers that are declared as a part of our tests
// (i.e. for testing one-offs).
afterEach(() => server.resetHandlers());
// Clean up after the tests are finished.
afterAll(() => server.close());
