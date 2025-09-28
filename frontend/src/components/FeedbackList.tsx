import { useState, useEffect } from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "./ui/badge";
import type { FeedbackPagedResponse } from "@/types/user";
import { getFeedbackByPage } from "@/api/userApi";

const PAGE_SIZE = 10;

function FeedbackList() {
  const [data, setData] = useState<FeedbackPagedResponse | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchFeedback = async () => {
      setIsLoading(true);
      try {
        const response = await getFeedbackByPage({
          pageNum: currentPage,
          pageSize: PAGE_SIZE,
        });
        setData(response.data);
      } catch (err) {
        console.error("Failed to fetch feedback:", err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchFeedback();
  }, [currentPage]);

  if (isLoading) {
    return (
      <div className="p-4 space-y-4">
        <Skeleton className="h-12 w-full" />
        {Array.from({ length: PAGE_SIZE }).map((_, index) => (
          <Skeleton key={index} className="h-10 w-full" />
        ))}
      </div>
    );
  }

  if (!data || data.records.length === 0) {
    return <div className="text-center text-gray-500 p-8">还没有用户反馈</div>;
  }

  const totalPages = data.totalPage;

  return (
    <div className="p-4">
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-[80px]">打分</TableHead>
              <TableHead>标题</TableHead>
              <TableHead className="hidden md:table-cell">内容</TableHead>
              <TableHead className="text-right hidden sm:table-cell">
                日期
              </TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {data.records.map((feedback) => (
              <TableRow key={feedback.id}>
                <TableCell>
                  <Badge
                    variant={feedback.rating < 3 ? "destructive" : "default"}
                  >
                    {feedback.rating} ★
                  </Badge>
                </TableCell>
                <TableCell>{feedback.title}</TableCell>
                <TableCell className="hidden md:table-cell max-w-xs truncate">
                  {feedback.content}
                </TableCell>
                <TableCell className="text-right hidden sm:table-cell">
                  {feedback.createTime}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      <div className="mt-4">
        <Pagination>
          <PaginationContent>
            <PaginationItem>
              <PaginationPrevious
                href="#"
                onClick={(e) => {
                  e.preventDefault();
                  setCurrentPage((prev) => Math.max(1, prev - 1));
                }}
                className={
                  currentPage === 1 ? "pointer-events-none opacity-50" : ""
                }
              />
            </PaginationItem>

            {currentPage > 2 && (
              <PaginationItem>
                <PaginationLink
                  href="#"
                  onClick={(e) => {
                    e.preventDefault();
                    setCurrentPage(1);
                  }}
                >
                  1
                </PaginationLink>
              </PaginationItem>
            )}
            {currentPage > 3 && (
              <PaginationItem>
                <PaginationEllipsis />
              </PaginationItem>
            )}

            {Array.from({ length: totalPages }, (_, i) => i + 1)
              .filter((page) => Math.abs(page - currentPage) <= 1)
              .map((page) => (
                <PaginationItem key={page}>
                  <PaginationLink
                    href="#"
                    onClick={(e) => {
                      e.preventDefault();
                      setCurrentPage(page);
                    }}
                    isActive={page === currentPage}
                  >
                    {page}
                  </PaginationLink>
                </PaginationItem>
              ))}

            {currentPage < totalPages - 2 && (
              <PaginationItem>
                <PaginationEllipsis />
              </PaginationItem>
            )}
            {currentPage < totalPages - 1 && (
              <PaginationItem>
                <PaginationLink
                  href="#"
                  onClick={(e) => {
                    e.preventDefault();
                    setCurrentPage(totalPages);
                  }}
                >
                  {totalPages}
                </PaginationLink>
              </PaginationItem>
            )}

            <PaginationItem>
              <PaginationNext
                href="#"
                onClick={(e) => {
                  e.preventDefault();
                  setCurrentPage((prev) => Math.min(totalPages, prev + 1));
                }}
                className={
                  currentPage === totalPages
                    ? "pointer-events-none opacity-50"
                    : ""
                }
              />
            </PaginationItem>
          </PaginationContent>
        </Pagination>
      </div>
    </div>
  );
}

export default FeedbackList;
