import { Card, CardContent, Typography } from '@mui/material';

interface Comment {
  id: number;
  text: string;
  author: { username: string };
  createdAt: string;
}

const CommentList = ({ comments }: { comments: Comment[] }) => {
  const sorted = [...comments].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  return (
    <>
      {sorted.map(comment => (
        <Card key={comment.id} sx={{ mb: 1 }}>
          <CardContent>
            <Typography variant="body2">{comment.text}</Typography>
            <Typography variant="caption" color="text.secondary">
              By {comment.author?.username} at {comment.createdAt}
            </Typography>
          </CardContent>
        </Card>
      ))}
    </>
  );
};

export default CommentList; 